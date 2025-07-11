@echo off
echo ==========================================
echo VS Code Java 调试修复脚本
echo ==========================================

echo.
echo 1. 检查项目结构...
if exist "pom.xml" (
    echo [OK] pom.xml 存在
) else (
    echo [FIXING] 复制 pom-standalone.xml 为 pom.xml...
    copy "pom-standalone.xml" "pom.xml"
)

echo.
echo 2. 编译项目...
mvn clean compile

echo.
echo 3. 测试简单类运行...
java -cp target/classes org.apache.olingo.sample.springboot.DebugTest

echo.
echo 4. 检查 VS Code 配置...
if exist ".vscode\launch.json" (
    echo [OK] launch.json 存在
) else (
    echo [ERROR] launch.json 不存在
)

if exist ".vscode\settings.json" (
    echo [OK] settings.json 存在
) else (
    echo [ERROR] settings.json 不存在
)

echo.
echo ==========================================
echo 修复完成！
echo.
echo 接下来的步骤：
echo 1. 在 VS Code 中按 Ctrl+Shift+P
echo 2. 输入 "Java: Reload Projects"
echo 3. 等待项目重新加载
echo 4. 打开 DebugTest.java 或 ODataSpringBootApplication.java
echo 5. 查看 main 方法上方是否出现 "Run | Debug" 链接
echo.
echo 如果仍然没有 "Run | Debug" 链接：
echo 1. 关闭 VS Code
echo 2. 双击 spring-boot-odata.code-workspace 文件
echo 3. 重新打开项目
echo.
pause
