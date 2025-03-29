package cn.onism.mcp.tool;

import lombok.Getter;
import lombok.Setter;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * 文件工具
 * 赋予大模型最基础的本地文件系统的
 *
 * @author Onism
 * @date 2025-03-24
 */
@Component
public class FileTool {

    private static final Set<String> WINDOWS_RESERVED_NAMES = Set.of(
            "CON", "PRN", "AUX", "NUL",
            "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"
    );

    @Tool(description = "读取指定文件内容")
    public ReadResponse readFile(ReadRequest request) {
        if (!isValidPath(request.filePath)) {
            return new ReadResponse(null, "文件读取失败，`" + request.filePath + "` 文件路径不合法");
        }
        try {
            String content = new String(Files.readAllBytes(Paths.get(request.getFilePath())));
            return new ReadResponse(content, null);
        } catch (IOException e) {
            return new ReadResponse(null, "文件读取失败: " + e.getMessage());
        }
    }

    @Tool(description = "写入内容到文件")
    public WriteResponse writeFile(WriteRequest request) {
        if (!isValidPath(request.filePath)) {
            return new WriteResponse(false, "文件读取失败，`" + request.filePath + "` 文件路径不合法");
        }
        try {
            OpenOption option = request.isAppend() ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING;
            Files.writeString(Path.of(request.getFilePath()),
                    request.getContent(),
                    StandardOpenOption.CREATE,
                    option);
            return new WriteResponse(true, null);
        } catch (IOException e) {
            return new WriteResponse(false, "文件写入失败: " + e.getMessage());
        }
    }

    @Tool(description = "创建目录")
    public WriteResponse createDirectory(DirectoryRequest request) {
        try {
            Files.createDirectories(Path.of(request.getDirPath()));
            return new WriteResponse(true, null);
        } catch (IOException e) {
            return new WriteResponse(false, "目录创建失败: " + e.getMessage());
        }
    }

    @Tool(description = "删除文件或目录")
    public WriteResponse deletePath(ReadRequest request) {
        if (!isValidPath(request.filePath)) {
            return new WriteResponse(false, "文件读取失败，`" + request.filePath + "` 文件路径不合法");
        }
        try {
            Path path = Path.of(request.getFilePath());
            if (Files.isDirectory(path)) {
                Files.walk(path)
                        .sorted(Comparator.reverseOrder())
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (IOException ignored) {
                            }
                        });
            } else {
                Files.delete(path);
            }
            return new WriteResponse(true, null);
        } catch (IOException e) {
            return new WriteResponse(false, "删除失败: " + e.getMessage());
        }
    }

    @Tool(description = "列出目录内容")
    public FileListResponse listDirectory(DirectoryRequest request) {
        if (!isValidPath(request.dirPath)) {
            return new FileListResponse(null, "文件读取失败，`" + request.dirPath + "` 文件路径不合法");
        }
        try (Stream<Path> stream = Files.list(Path.of(request.getDirPath()))) {
            List<String> files = stream
                    .filter(p -> !p.getFileName().toString().startsWith("."))
                    .map(p -> p.getFileName().toString())
                    .toList();
            return new FileListResponse(files, null);
        } catch (IOException e) {
            return new FileListResponse(null, "目录读取失败: " + e.getMessage());
        }
    }

    public static boolean isValidPath(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        // 基础路径格式校验
        try {
            Paths.get(path);
        } catch (InvalidPathException ex) {
            return false;
        }

        // 获取操作系统类型
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

        // Windows专属校验
        if (isWindows) {
            try {
                Path pathObj = Paths.get(path);
                for (Path component : pathObj) {
                    String fileName = component.toString().trim();
                    if (fileName.isEmpty()) continue;

                    // 保留名称检查
                    if (isReservedName(fileName)) {
                        return false;
                    }

                    // 非法结尾检查（空格或点）
                    if (fileName.matches(".*[. ]$")) {
                        return false;
                    }

                    // 文件名长度限制（255字符）
                    if (fileName.length() > 255) {
                        return false;
                    }
                }
            } catch (InvalidPathException ex) {
                return false; // 冗余校验确保异常捕获
            }
        }

        return true;
    }

    private static boolean isReservedName(String fileName) {
        String upperName = fileName.toUpperCase();
        int dotIndex = upperName.indexOf('.');
        String baseName = (dotIndex == -1) ? upperName : upperName.substring(0, dotIndex);
        return WINDOWS_RESERVED_NAMES.contains(baseName);
    }

    /**
     * 文件读取请求
     *
     * @author Onism
     * @date 2025-03-24
     */
    @Setter
    @Getter
    static class ReadRequest {
        /**
         * 文件路径
         */
        private String filePath;
        /**
         * 文件名
         */
        private  String fileName;

        public String getFilePath() {
            return filePath + fileName;
        }
    }

    /**
     * 写入请求
     *
     * @author Onism
     * @date 2025-03-24
     */
    @Setter
    @Getter
    static class WriteRequest {
        /**
         * 文件路径
         */
        private String filePath;

        /**
         * 文件名
         */
        private String fileName;
        /**
         * 内容
         */
        private String content;
        /**
         * 是否为追加
         */
        private boolean append;


        /**
         * 获取文件路径
         *
         * @return {@link String }
         */
        public String getFilePath() {
            return filePath + fileName;
        }
    }

    @Getter
    @Setter
    static final class ReadResponse {
        private String content;
        private String error;


        public ReadResponse(String content, String error) {
            this.content = content;
            this.error = error;
        }

    }

    @Setter
    @Getter
    static
    class WriteResponse {
        private boolean success;
        private String error;

        public WriteResponse(boolean success, String error) {
            this.success = success;
            this.error = error;
        }

    }

    /**
     * 文件列表响应
     *
     * @author Onism
     * @date 2025-03-28
     */
    @Getter
    @Setter
    static class FileListResponse {
        /**
         * 文件列表
         */
        private List<String> files;
        /**
         * 错误信息
         */
        private String error;

        public FileListResponse(List<String> files, String error) {
            this.files = files;
            this.error = error;
        }
    }

    @Setter
    @Getter
    static
    class DirectoryRequest {
        private String dirPath;
    }
}
