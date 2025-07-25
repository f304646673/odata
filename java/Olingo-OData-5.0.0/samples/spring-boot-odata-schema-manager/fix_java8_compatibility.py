#!/usr/bin/env python3
import os
import re
import glob

def fix_java8_compatibility(file_path):
    """修复Java 8兼容性问题"""
    print(f"Processing: {file_path}")
    
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    original_content = content
    
    # 添加必要的导入
    if 'import java.util.Arrays;' not in content and ('List.of(' in content or 'Arrays.asList(' in content):
        # 查找java.util导入的位置
        import_pattern = r'(import java\.util\.[^;]+;)'
        imports = re.findall(import_pattern, content)
        if imports and 'import java.util.Arrays;' not in content:
            # 在第一个java.util导入后添加Arrays导入
            first_util_import = imports[0]
            content = content.replace(first_util_import, first_util_import + '\nimport java.util.Arrays;')
    
    # 替换 List.of() 为 Arrays.asList()
    content = re.sub(r'List\.of\((.*?)\)', r'Arrays.asList(\1)', content)
    
    # 替换 Map.of() 为手动创建的HashMap
    # 简单情况：Map.of(key, value)
    def replace_map_of_simple(match):
        args = match.group(1).strip()
        if not args:  # Map.of()
            return 'new HashMap<>()'
        
        # 分割参数，注意要处理嵌套的泛型和方法调用
        pairs = []
        paren_level = 0
        current_arg = ''
        args_list = []
        
        for char in args:
            if char == '(' or char == '<':
                paren_level += 1
            elif char == ')' or char == '>':
                paren_level -= 1
            elif char == ',' and paren_level == 0:
                args_list.append(current_arg.strip())
                current_arg = ''
                continue
            current_arg += char
        
        if current_arg.strip():
            args_list.append(current_arg.strip())
        
        if len(args_list) % 2 != 0:
            return f'Map.of({args})'  # 如果参数数量不是偶数，保持原样
        
        # 生成HashMap初始化代码
        result = 'new HashMap<String, CsdlSchema>() {{\n'
        for i in range(0, len(args_list), 2):
            key = args_list[i]
            value = args_list[i + 1]
            result += f'            put({key}, {value});\n'
        result += '        }}'
        return result
    
    content = re.sub(r'Map\.of\((.*?)\)', replace_map_of_simple, content, flags=re.DOTALL)
    
    # 替换 var 关键字
    # 查找 var 声明并替换为具体类型
    var_patterns = [
        (r'\bvar\s+(\w+)\s*=\s*loadLargeSchema\(\);', r'CsdlSchema \1 = loadLargeSchema();'),
        (r'\bvar\s+(\w+)\s*=\s*loadCircularDependencySchema\(\);', r'CsdlSchema \1 = loadCircularDependencySchema();'),
        (r'\bvar\s+(\w+)\s*=\s*loadMultiDependencySchema\(\);', r'CsdlSchema \1 = loadMultiDependencySchema();'),
        (r'\bvar\s+(\w+)\s*=\s*load\w*Schema\(\);', r'CsdlSchema \1 = load\w*Schema();'),
        (r'\bvar\s+(\w+)\s*=\s*([^;]+;)', r'CsdlSchema \1 = \2'),
    ]
    
    for pattern, replacement in var_patterns:
        content = re.sub(pattern, replacement, content)
    
    # 修复缺少的导入语句中的List变量引用问题
    content = re.sub(r'(\s+)List\s+(\w+)', r'\1List<String> \2', content)
    
    # 保存修改后的文件
    if content != original_content:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Fixed: {file_path}")
        return True
    else:
        print(f"No changes needed: {file_path}")
        return False

def main():
    # 查找所有测试文件
    test_files = glob.glob('src/test/java/**/*.java', recursive=True)
    
    fixed_count = 0
    for file_path in test_files:
        if fix_java8_compatibility(file_path):
            fixed_count += 1
    
    print(f"\n修复完成！共修复了 {fixed_count} 个文件")

if __name__ == '__main__':
    main()
