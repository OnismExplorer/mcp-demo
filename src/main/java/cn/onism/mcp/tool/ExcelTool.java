package cn.onism.mcp.tool;

import cn.onism.mcp.annotations.McpTool;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Excel 工具
 * 赋予大模型使用 Apache POI 操作 Excel(.xlsx) 文件的能力
 *
 * @author Onism
 * @date 2025-07-18
 */
@Component
@McpTool
public class ExcelTool {

    @Tool(description = "创建或写入一个 Excel (.xlsx) 文件数据应为二维列表，例如 [['姓名'], ['张三']]注意：此操作会覆盖整个工作表")
    public ExcelWriteResponse writeExcel(WriteExcelRequest request) {
        return updateWorkbook(request.getFilePath(), false, workbook -> {
            Sheet sheet = workbook.createSheet(request.getSheetName());
            List<List<Object>> data = request.getData();
            for (int i = 0; i < data.size(); i++) {
                Row row = sheet.createRow(i);
                List<Object> rowData = data.get(i);
                for (int j = 0; j < rowData.size(); j++) {
                    Cell cell = row.createCell(j);
                    setCellValue(cell, rowData.get(j));
                }
            }
        });
    }

    @Tool(description = "向现有工作表的末尾追加多行数据文件和工作表必须已存在")
    public ExcelWriteResponse appendRows(WriteExcelRequest request) {
        return updateWorkbook(request.getFilePath(), true, workbook -> {
            Sheet sheet = getSheet(workbook, request.getSheetName());
            int lastRowNum = sheet.getLastRowNum();
            // 如果工作表为空，追加应从第0行开始
            int startRow = (sheet.getPhysicalNumberOfRows() == 0) ? 0 : lastRowNum + 1;
            List<List<Object>> data = request.getData();
            for (int i = 0; i < data.size(); i++) {
                Row row = sheet.createRow(startRow + i);
                List<Object> rowData = data.get(i);
                for (int j = 0; j < rowData.size(); j++) {
                    Cell cell = row.createCell(j);
                    setCellValue(cell, rowData.get(j));
                }
            }
        });
    }

    @Tool(description = "读取 Excel (.xlsx) 文件的指定工作表内容，并将其作为二维字符串列表返回")
    public ExcelReadResponse readExcel(SheetRequest request) {
        File file = new File(request.getFilePath());
        if (!file.exists()) return new ExcelReadResponse(null, "文件不存在: " + request.getFilePath());

        try (FileInputStream fis = new FileInputStream(file); Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = getSheet(workbook, request.getSheetName());
            DataFormatter dataFormatter = new DataFormatter();
            List<List<String>> data = new ArrayList<>();
            for (Row row : sheet) {
                List<String> rowData = new ArrayList<>();
                for (Cell cell : row) {
                    rowData.add(dataFormatter.formatCellValue(cell));
                }
                data.add(rowData);
            }
            return new ExcelReadResponse(data, null);
        } catch (Exception e) {
            return new ExcelReadResponse(null, "读取 Excel 文件失败: " + e.getMessage());
        }
    }

    @Tool(description = "列出指定 Excel 文件中所有工作表的名称")
    public SheetListResponse listSheets(FilePathRequest request) {
        File file = new File(request.getFilePath());
        if (!file.exists()) return new SheetListResponse(null, "文件不存在: " + request.getFilePath());
        try (FileInputStream fis = new FileInputStream(file); Workbook workbook = new XSSFWorkbook(fis)) {
            int sheetCount = workbook.getNumberOfSheets();
            List<String> sheetNames = new ArrayList<>(sheetCount);
            for (int i = 0; i < sheetCount; i++) {
                sheetNames.add(workbook.getSheetName(i));
            }
            return new SheetListResponse(sheetNames, null);
        } catch (Exception e) {
            return new SheetListResponse(null, "列出工作表失败: " + e.getMessage());
        }
    }

    @Tool(description = "在指定的 Excel 文件中创建一个新的、空的工作表如果工作表已存在，则不会执行任何操作")
    public ExcelWriteResponse createSheet(SheetRequest request) {
        return updateWorkbook(request.getFilePath(), true, workbook -> {
            if (workbook.getSheet(request.getSheetName()) == null) {
                workbook.createSheet(request.getSheetName());
            }
        });
    }

    @Tool(description = "重命名一个现有的工作表")
    public ExcelWriteResponse renameSheet(RenameSheetRequest request) {
        return updateWorkbook(request.getFilePath(), true, workbook -> {
            int sheetIndex = workbook.getSheetIndex(request.getOldSheetName());
            if (sheetIndex == -1)
                throw new IllegalArgumentException("工作表 '" + request.getOldSheetName() + "' 不存在");
            workbook.setSheetName(sheetIndex, request.getNewSheetName());
        });
    }

    @Tool(description = "从 Excel 文件中删除一个工作表")
    public ExcelWriteResponse deleteSheet(SheetRequest request) {
        return updateWorkbook(request.getFilePath(), true, workbook -> {
            int sheetIndex = workbook.getSheetIndex(request.getSheetName());
            if (sheetIndex == -1) throw new IllegalArgumentException("工作表 '" + request.getSheetName() + "' 不存在");
            workbook.removeSheetAt(sheetIndex);
        });
    }
    

    @Tool(description = "修改 Excel 单元格的样式，包括字体、颜色、边框和对齐方式可以只提供要更改的属性")
    public ExcelWriteResponse setCellStyle(SetCellStyleRequest request) {
        return updateCell(request, cell -> {
            Workbook workbook = cell.getSheet().getWorkbook();
            CellStyle newStyle = workbook.createCellStyle();
            newStyle.cloneStyleFrom(cell.getCellStyle());

            if (request.hasFontChanges()) {
                Font newFont = workbook.createFont();
                Font currentFont = workbook.getFontAt(cell.getCellStyle().getFontIndex());
                newFont.setFontName(Objects.requireNonNullElse(request.getFontName(), currentFont.getFontName()));
                newFont.setFontHeightInPoints(Objects.requireNonNullElse(request.getFontHeightInPoints(), currentFont.getFontHeightInPoints()));
                newFont.setBold(Objects.requireNonNullElse(request.getBold(), currentFont.getBold()));
                if (request.getFontColor() != null)
                    newFont.setColor(IndexedColors.valueOf(request.getFontColor().toUpperCase()).getIndex());
                else newFont.setColor(currentFont.getColor());
                newStyle.setFont(newFont);
            }
            if (request.getFillBackgroundColor() != null) {
                newStyle.setFillForegroundColor(IndexedColors.valueOf(request.getFillBackgroundColor().toUpperCase()).getIndex());
                newStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            }
            if (request.getBorderType() != null) {
                BorderStyle borderStyle = BorderStyle.valueOf(request.getBorderType().toUpperCase());
                newStyle.setBorderTop(borderStyle);
                newStyle.setBorderBottom(borderStyle);
                newStyle.setBorderLeft(borderStyle);
                newStyle.setBorderRight(borderStyle);
            }
            if (request.getHorizontalAlignment() != null)
                newStyle.setAlignment(HorizontalAlignment.valueOf(request.getHorizontalAlignment().toUpperCase()));
            if (request.getVerticalAlignment() != null)
                newStyle.setVerticalAlignment(VerticalAlignment.valueOf(request.getVerticalAlignment().toUpperCase()));

            cell.setCellStyle(newStyle);
        });
    }

    @Tool(description = "合并单元格指定要合并的矩形区域的起始和结束的行和列索引")
    public ExcelWriteResponse mergeCells(MergeCellsRequest request) {
        return updateWorkbook(request.getFilePath(), true, workbook -> {
            Sheet sheet = getSheet(workbook, request.getSheetName());
            sheet.addMergedRegion(new CellRangeAddress(
                    request.getFirstRow(), request.getLastRow(),
                    request.getFirstColumn(), request.getLastColumn()
            ));
        });
    }

    @Tool(description = "设置指定列的宽度宽度单位大约等于字符数")
    public ExcelWriteResponse setColumnWidth(SetColumnWidthRequest request) {
        return updateWorkbook(request.getFilePath(), true, workbook -> {
            Sheet sheet = getSheet(workbook, request.getSheetName());
            sheet.setColumnWidth(request.getColumnIndex(), request.getWidth() * 256);
        });
    }

    @Tool(description = "自动调整指定列的宽度以适应其内容")
    public ExcelWriteResponse autoSizeColumn(ColumnRequest request) {
        return updateWorkbook(request.getFilePath(), true, workbook -> {
            Sheet sheet = getSheet(workbook, request.getSheetName());
            sheet.autoSizeColumn(request.getColumnIndex());
        });
    }

    @Tool(description = "设置指定行的高度高度单位是“磅”(points)")
    public ExcelWriteResponse setRowHeight(SetRowHeightRequest request) {
        return updateWorkbook(request.getFilePath(), true, workbook -> {
            Sheet sheet = getSheet(workbook, request.getSheetName());
            Row row = getOrCreateRow(sheet, request.getRowIndex());
            row.setHeightInPoints(request.getHeightInPoints());
        });
    }

    @Tool(description = "删除指定行，并将其下方的所有行向上移动一行")
    public ExcelWriteResponse deleteRow(RowRequest request) {
        return updateWorkbook(request.getFilePath(), true, workbook -> {
            Sheet sheet = getSheet(workbook, request.getSheetName());
            int rowIndex = request.getRowIndex();
            int lastRowNum = sheet.getLastRowNum();
            Row rowToRemove = sheet.getRow(rowIndex);
            if (rowToRemove != null) {
                sheet.removeRow(rowToRemove);
            }
            if (rowIndex >= 0 && rowIndex < lastRowNum) {
                sheet.shiftRows(rowIndex + 1, lastRowNum, -1);
            }
        });
    }

    @Tool(description = "在指定单元格中设置一个 Excel 公式，例如 'SUM(A1:A10)'")
    public ExcelWriteResponse setCellFormula(SetFormulaRequest request) {
        return updateCell(request, cell -> cell.setCellFormula(request.getFormula()));
    }

    @Tool(description = "向 Excel 工作表添加图片图片将根据给定的起始和结束单元格进行定位和缩放")
    public ExcelWriteResponse addPicture(AddPictureRequest request) {
        return updateWorkbook(request.getExcelFilePath(), true, workbook -> {
            try (FileInputStream imageFis = new FileInputStream(request.getImagePath())) {
                byte[] bytes = imageFis.readAllBytes();
                int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
                Sheet sheet = getSheet(workbook, request.getSheetName());
                Drawing<?> drawing = sheet.createDrawingPatriarch();
                ClientAnchor anchor = workbook.getCreationHelper().createClientAnchor();
                anchor.setCol1(request.getStartColumn());
                anchor.setRow1(request.getStartRow());
                anchor.setCol2(request.getEndColumn());
                anchor.setRow2(request.getEndRow());
                drawing.createPicture(anchor, pictureIdx);
            } catch (IOException e) {
                throw new RuntimeException("读取图片文件失败: " + e.getMessage(), e);
            }
        });
    }

    private ExcelWriteResponse updateWorkbook(String filePath, boolean fileMustExist, WorkbookUpdater updater) {
        File file = new File(filePath);
        if (fileMustExist && !file.exists()) {
            return new ExcelWriteResponse(false, "文件不存在: " + filePath);
        }

        try (Workbook workbook = fileMustExist ? new XSSFWorkbook(new FileInputStream(file)) : new XSSFWorkbook()) {
            updater.update(workbook);
            saveWorkbook(workbook, filePath);
            return new ExcelWriteResponse(true, null);
        } catch (Exception e) {
            return new ExcelWriteResponse(false, "操作 Excel 文件失败: " + e.getMessage());
        }
    }

    private ExcelWriteResponse updateCell(CellRequest request, Consumer<Cell> cellConsumer) {
        return updateWorkbook(request.getFilePath(), true, workbook -> {
            Sheet sheet = getSheet(workbook, request.getSheetName());
            Row row = getOrCreateRow(sheet, request.getRowIndex());
            Cell cell = getOrCreateCell(row, request.getColumnIndex());
            cellConsumer.accept(cell);
        });
    }

    private void setCellValue(Cell cell, Object o) {
        if (o instanceof String value) cell.setCellValue(value);
        else if (o instanceof Number value) cell.setCellValue(value.doubleValue());
        else if (o instanceof Boolean value) cell.setCellValue(value);
        else if (o != null) cell.setCellValue(o.toString());
    }

    private void saveWorkbook(Workbook workbook, String filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            workbook.write(fos);
        }
    }

    private Sheet getSheet(Workbook workbook, String sheetName) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) throw new IllegalArgumentException("工作表 '" + sheetName + "' 不存在");
        return sheet;
    }

    private Row getOrCreateRow(Sheet sheet, int rowIndex) {
        Row row = sheet.getRow(rowIndex);
        return (row != null) ? row : sheet.createRow(rowIndex);
    }

    private Cell getOrCreateCell(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex);
        return (cell != null) ? cell : row.createCell(colIndex);
    }

    @FunctionalInterface
    private interface WorkbookUpdater {
        void update(Workbook workbook);
    }

    @Setter
    @Getter
    public static class FilePathRequest {
        @ToolParam(description = "目标 Excel 文件的完整路径")
        private String filePath;
    }

    @Setter
    @Getter
    public static class SheetRequest extends FilePathRequest {
        @ToolParam(description = "工作表名称")
        private String sheetName;
    }

    @Setter
    @Getter
    public static class RowRequest extends SheetRequest {
        @ToolParam(description = "目标行的索引（从0开始）")
        private int rowIndex;
    }

    @Setter
    @Getter
    public static class ColumnRequest extends SheetRequest {
        @ToolParam(description = "目标列的索引（从0开始）")
        private int columnIndex;
    }

    @Setter
    @Getter
    public static class CellRequest extends RowRequest {
        @ToolParam(description = "目标单元格的列索引（从0开始）")
        private int columnIndex;
    }

    @Setter
    @Getter
    public static class WriteExcelRequest extends SheetRequest {
        @ToolParam(description = "要写入的二维数据外层列表代表行，内层列表代表该行的单元格")
        private List<List<Object>> data;
    }

    @Setter
    @Getter
    public static class SetFormulaRequest extends CellRequest {
        @ToolParam(description = "要设置的 Excel 公式字符串，例如 'SUM(A1:A10)'")
        private String formula;
    }

    @Setter
    @Getter
    public static class SetRowHeightRequest extends RowRequest {
        @ToolParam(description = "要设置的高度值，单位是“磅”（points），例如 20.5")
        private float heightInPoints;
    }

    @Setter
    @Getter
    public static class SetColumnWidthRequest extends ColumnRequest {
        @ToolParam(description = "要设置的宽度值，大约等于字符数例如，15 表示宽度为 15 个字符")
        private int width;
    }

    @Setter
    @Getter
    public static class RenameSheetRequest extends FilePathRequest {
        @ToolParam(description = "当前的工作表名称")
        private String oldSheetName;
        @ToolParam(description = "新的工作表名称")
        private String newSheetName;
    }

    @Setter
    @Getter
    public static class MergeCellsRequest extends SheetRequest {
        @ToolParam(description = "合并区域的起始行索引")
        private int firstRow;
        @ToolParam(description = "合并区域的结束行索引")
        private int lastRow;
        @ToolParam(description = "合并区域的起始列索引")
        private int firstColumn;
        @ToolParam(description = "合并区域的结束列索引")
        private int lastColumn;
    }

    @Setter
    @Getter
    public static class SetCellStyleRequest extends CellRequest {
        @ToolParam(description = "字体名称，例如 'Arial' 或 'Calibri'")
        private String fontName;
        @ToolParam(description = "字体大小（磅）")
        private Short fontHeightInPoints;
        @ToolParam(description = "是否加粗")
        private Boolean bold;
        @ToolParam(description = "字体颜色必须是 POI IndexedColors 的名称，例如 'RED', 'BLUE'")
        private String fontColor;
        @ToolParam(description = "单元格背景填充色必须是 POI IndexedColors 的名称")
        private String fillBackgroundColor;
        @ToolParam(description = "单元格边框样式必须是 POI BorderStyle 的名称，例如 'THIN'")
        private String borderType;
        @ToolParam(description = "水平对齐方式必须是 POI HorizontalAlignment 的名称，例如 'CENTER'")
        private String horizontalAlignment;
        @ToolParam(description = "垂直对齐方式必须是 POI VerticalAlignment 的名称，例如 'CENTER'")
        private String verticalAlignment;

        public boolean hasFontChanges() {
            return fontName != null || fontHeightInPoints != null || bold != null || fontColor != null;
        }
    }

    @Setter
    @Getter
    public static class AddPictureRequest {
        @ToolParam(description = "目标 Excel 文件的完整路径")
        private String excelFilePath;
        @ToolParam(description = "要添加的本地图片文件的完整路径")
        private String imagePath;
        @ToolParam(description = "工作表名称")
        private String sheetName;
        @ToolParam(description = "图片左上角所在的行索引（从0开始）")
        private int startRow;
        @ToolParam(description = "图片左上角所在的列索引（从0开始）")
        private int startColumn;
        @ToolParam(description = "图片右下角所在的行索引（从0开始）")
        private int endRow;
        @ToolParam(description = "图片右下角所在的列索引（从0开始）")
        private int endColumn;
    }

    @Getter
    @Setter
    public static final class ExcelReadResponse {
        private List<List<String>> data;
        private String error;

        public ExcelReadResponse(List<List<String>> data, String error) {
            this.data = data;
            this.error = error;
        }
    }

    @Getter
    @Setter
    public static final class ExcelWriteResponse {
        private boolean success;
        private String error;

        public ExcelWriteResponse(boolean success, String error) {
            this.success = success;
            this.error = error;
        }
    }

    @Getter
    @Setter
    public static final class SheetListResponse {
        private List<String> sheetNames;
        private String error;

        public SheetListResponse(List<String> sheetNames, String error) {
            this.sheetNames = sheetNames;
            this.error = error;
        }
    }
}
