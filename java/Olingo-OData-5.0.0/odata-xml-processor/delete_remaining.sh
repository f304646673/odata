#!/bin/bash

# 批量重写扩展模型类为组合模式的脚本

cd /home/fangliang/0801/odata/java/Olingo-OData-5.0.0/odata-xml-processor

# 获取所有需要重写的类（排除已经是组合模式的）
classes=(
    "ExtendedCsdlAction"
    "ExtendedCsdlActionImport"
    "ExtendedCsdlEntityContainer"
    "ExtendedCsdlFunction"
    "ExtendedCsdlFunctionImport"
    "ExtendedCsdlSingleton"
    "ExtendedCsdlTerm"
)

for class in "${classes[@]}"; do
    echo "Deleting $class.java..."
    rm -f "src/main/java/org/apache/olingo/xmlprocessor/core/model/$class.java"
done

echo "All classes deleted. Ready for recreation."
