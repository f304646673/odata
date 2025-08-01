package org.apache.olingo.schema.validation.engine;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OData Schema验证引擎主程序
 * 提供命令行界面执行验证和集成流程
 */
public class ODataSchemaValidationEngineMain {
    
    private static final Logger logger = LoggerFactory.getLogger(ODataSchemaValidationEngineMain.class);
    
    public static void main(String[] args) {
        logger.info("=== OData Schema Validation Engine ===");
        
        if (args.length > 0) {
            // 命令行模式
            processCommandLine(args);
        } else {
            // 交互模式
            runInteractiveMode();
        }
    }
    
    /**
     * 处理命令行参数
     */
    private static void processCommandLine(String[] args) {
        if (args.length < 1) {
            printUsage();
            return;
        }
        
        String xmlDirectoryPath = args[0];
        
        try (ODataSchemaValidationEngine engine = new ODataSchemaValidationEngine()) {
            Path directoryPath = Paths.get(xmlDirectoryPath);
            IntegrationResult result = engine.processDirectory(directoryPath);
            printResult(result);
            
            // 设置退出代码
            if (result.isError()) {
                System.exit(1);
            } else if (result.isValidationFailure()) {
                System.exit(1);
            } else {
                System.exit(0);
            }
            
        } catch (Exception e) {
            logger.error("处理过程中发生错误", e);
            System.exit(1);
        }
    }
    
    /**
     * 交互模式
     */
    private static void runInteractiveMode() {
        Scanner scanner = new Scanner(System.in);
        
        try (ODataSchemaValidationEngine engine = new ODataSchemaValidationEngine()) {
            
            while (true) {
                System.out.println("\n=== OData Schema验证引擎 - 交互模式 ===");
                System.out.println("1. 验证XML目录");
                System.out.println("2. 退出");
                System.out.print("请选择操作 (1-2): ");
                
                String choice = scanner.nextLine().trim();
                
                switch (choice) {
                    case "1":
                        processDirectoryValidation(scanner, engine);
                        break;
                    case "2":
                        System.out.println("退出程序。");
                        return;
                    default:
                        System.out.println("无效选择，请重试。");
                }
            }
            
        } catch (Exception e) {
            logger.error("交互模式运行时发生错误", e);
        } finally {
            scanner.close();
        }
    }
    
    /**
     * 处理目录验证
     */
    private static void processDirectoryValidation(Scanner scanner, ODataSchemaValidationEngine engine) {
        System.out.print("请输入XML目录路径: ");
        String directoryPath = scanner.nextLine().trim();
        
        if (directoryPath.isEmpty()) {
            System.out.println("目录路径不能为空。");
            return;
        }
        
        try {
            Path path = Paths.get(directoryPath);
            System.out.println("正在处理目录: " + path.toAbsolutePath());
            
            IntegrationResult result = engine.processDirectory(path);
            printResult(result);
            
        } catch (Exception e) {
            System.err.println("处理目录时发生错误: " + e.getMessage());
            logger.debug("详细错误信息", e);
        }
    }
    
    /**
     * 打印处理结果
     */
    private static void printResult(IntegrationResult result) {
        System.out.println("\n=== 处理结果 ===");
        System.out.println("状态: " + result.getStatus());
        
        if (result.getMessage() != null) {
            System.out.println("消息: " + result.getMessage());
        }
        
        if (result.isSuccess()) {
            System.out.println("✓ 处理成功完成");
            System.out.printf("文件统计: 总计 %d 个，有效 %d 个，无效 %d 个%n", 
                result.getTotalFiles(), result.getValidFiles(), result.getInvalidFiles());
            
            if (result.getProcessingTime() > 0) {
                System.out.println("处理时间: " + result.getProcessingTime() + " ms");
            }
            
        } else if (result.isValidationFailure()) {
            System.out.printf("验证结果: 失败 (处理时间: %d ms)%n", result.getProcessingTime());
            if (!result.getValidationErrors().isEmpty()) {
                System.out.println("验证错误:");
                for (String error : result.getValidationErrors()) {
                    System.out.println("  - " + error);
                }
            }
            System.out.println("✗ 验证失败，请检查XML文件的合规性");
            
        } else if (result.hasConflicts()) {
            System.out.println("✗ 检测到Schema冲突");
            if (!result.getConflicts().isEmpty()) {
                System.out.println("冲突详情:");
                for (String conflict : result.getConflicts()) {
                    System.out.println("  - " + conflict);
                }
            }
            
        } else if (result.isError()) {
            System.out.println("✗ 处理过程中发生错误");
        }
    }
    
    /**
     * 打印使用说明
     */
    private static void printUsage() {
        System.out.println("使用方法:");
        System.out.println("  java ODataSchemaValidationEngineMain <XML目录路径>");
        System.out.println("  java ODataSchemaValidationEngineMain  # 进入交互模式");
        System.out.println();
        System.out.println("示例:");
        System.out.println("  java ODataSchemaValidationEngineMain /path/to/xml/directory");
    }
}
