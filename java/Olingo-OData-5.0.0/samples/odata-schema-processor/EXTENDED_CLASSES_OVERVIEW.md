# OData Schema依赖分析扩展类总览

## 概述

本项目对Apache Olingo框架中的核心OData Schema元素进行了系统化扩展，为每个可能引用其他类型的CSDL元素增加了依赖关系追踪功能。这样的深度扩展确保了完整的依赖分析能力，覆盖OData 4.0规范中所有可能存在引用关系的元素。

## 扩展的CSDL元素类型

### 1. 类型定义类 (Type Definition Classes)

#### ExtendedCsdlEntityType
- **基础类**: `org.apache.olingo.commons.api.edm.provider.CsdlEntityType`
- **依赖来源**:
  - BaseType: 继承的实体类型
  - Properties: 属性类型引用
  - NavigationProperties: 导航属性类型引用
- **自动依赖分析**: setBaseType()时触发

#### ExtendedCsdlComplexType
- **基础类**: `org.apache.olingo.commons.api.edm.provider.CsdlComplexType`
- **依赖来源**:
  - BaseType: 继承的复杂类型
  - Properties: 属性类型引用
- **自动依赖分析**: setBaseType()时触发

#### ExtendedCsdlProperty
- **基础类**: `org.apache.olingo.commons.api.edm.provider.CsdlProperty`
- **依赖来源**:
  - Type: 属性的数据类型
- **特殊处理**: 支持Collection包装类型

#### ExtendedCsdlNavigationProperty
- **基础类**: `org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty`
- **依赖来源**:
  - Type: 导航到的实体类型
- **特殊处理**: 支持Collection包装类型

### 2. 操作定义类 (Operation Definition Classes)

#### ExtendedCsdlAction
- **基础类**: `org.apache.olingo.commons.api.edm.provider.CsdlAction`
- **依赖来源**:
  - Parameters: 参数类型引用
  - ReturnType: 返回类型引用
- **分析方法**: analyzeDependencies()

#### ExtendedCsdlFunction
- **基础类**: `org.apache.olingo.commons.api.edm.provider.CsdlFunction`
- **依赖来源**:
  - Parameters: 参数类型引用
  - ReturnType: 返回类型引用
- **分析方法**: analyzeDependencies()

#### ExtendedCsdlParameter
- **基础类**: `org.apache.olingo.commons.api.edm.provider.CsdlParameter`
- **依赖来源**:
  - Type: 参数类型
- **自动依赖分析**: setType()时触发

#### ExtendedCsdlReturnType
- **基础类**: `org.apache.olingo.commons.api.edm.provider.CsdlReturnType`
- **依赖来源**:
  - Type: 返回值类型
- **自动依赖分析**: setType()时触发

### 3. 容器定义类 (Container Definition Classes)

#### ExtendedCsdlEntitySet
- **基础类**: `org.apache.olingo.commons.api.edm.provider.CsdlEntitySet`
- **依赖来源**:
  - Type: 实体集的实体类型
  - NavigationPropertyBindings: 导航属性绑定的目标
- **自动依赖分析**: setType()时触发

#### ExtendedCsdlSingleton
- **基础类**: `org.apache.olingo.commons.api.edm.provider.CsdlSingleton`
- **依赖来源**:
  - Type: Singleton的实体类型
  - NavigationPropertyBindings: 导航属性绑定的目标
- **自动依赖分析**: setType()时触发

#### ExtendedCsdlActionImport
- **基础类**: `org.apache.olingo.commons.api.edm.provider.CsdlActionImport`
- **依赖来源**:
  - Action: 引用的Action
  - EntitySet: 相关的实体集
- **自动依赖分析**: setAction()或setEntitySet()时触发

#### ExtendedCsdlFunctionImport
- **基础类**: `org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport`
- **依赖来源**:
  - Function: 引用的Function
  - EntitySet: 相关的实体集
- **自动依赖分析**: setFunction()或setEntitySet()时触发

### 4. 类型别名类 (Type Alias Classes)

#### ExtendedCsdlTypeDefinition
- **基础类**: `org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition`
- **依赖来源**:
  - UnderlyingType: 基础类型
- **自动依赖分析**: setUnderlyingType()时触发

## 共同特性

### 依赖管理方法
所有扩展类都提供以下核心方法：
- `addDependency(String namespace)`: 添加依赖
- `removeDependency(String namespace)`: 移除依赖
- `getDependencies()`: 获取所有依赖
- `hasDependency(String namespace)`: 检查依赖存在性
- `clearDependencies()`: 清除所有依赖
- `getDependencyCount()`: 获取依赖数量

### Namespace提取逻辑
所有类都使用统一的namespace提取逻辑：
1. **Collection类型处理**: `Collection(Type)` → `Type`
2. **EDM类型过滤**: 忽略`Edm.`开头的基础类型
3. **Namespace提取**: 从完全限定名中提取namespace部分
4. **空值处理**: 自动忽略null、空字符串和空白字符串

### 流式接口支持
所有setter方法都返回扩展类实例，支持方法链式调用：
```java
ExtendedCsdlComplexType complexType = new ExtendedCsdlComplexType()
    .setName("ComplexType")
    .setBaseType("com.example.BaseType")
    .setAbstract(true);
```

### 自动依赖分析
许多类在关键属性设置时会自动触发依赖分析：
- `ExtendedCsdlEntityType`: setBaseType()时
- `ExtendedCsdlComplexType`: setBaseType()时
- `ExtendedCsdlParameter`: setType()时
- `ExtendedCsdlReturnType`: setType()时
- `ExtendedCsdlEntitySet`: setType()时
- `ExtendedCsdlSingleton`: setType()时
- `ExtendedCsdlActionImport`: setAction()或setEntitySet()时
- `ExtendedCsdlFunctionImport`: setFunction()或setEntitySet()时
- `ExtendedCsdlTypeDefinition`: setUnderlyingType()时

## 测试覆盖

项目包含全面的单元测试：
- **ExtendedCsdlTypesTest**: 综合功能测试
- **基本依赖管理测试**: 添加、移除、清除、查询依赖
- **流式接口测试**: 方法链调用验证
- **Namespace提取测试**: 各种类型格式的正确处理
- **边界条件测试**: null值、空字符串、EDM类型的处理

## 架构优势

1. **完整性**: 覆盖了OData 4.0规范中所有可能引用其他类型的元素
2. **一致性**: 所有扩展类使用统一的依赖管理接口和实现
3. **自动化**: 关键操作触发自动依赖分析，减少手动管理
4. **可扩展性**: 易于添加新的依赖分析逻辑
5. **性能**: 使用HashSet存储依赖，查询和操作效率高
6. **类型安全**: 强类型设计，编译时错误检查
7. **可测试性**: 完整的单元测试覆盖，确保功能正确性

这个深度扩展的依赖分析系统为OData Schema的完整依赖管理提供了坚实的基础，支持复杂的Schema分析、验证和优化场景。
