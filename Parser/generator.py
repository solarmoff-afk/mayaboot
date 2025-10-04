import json
import os
import sys
import subprocess

CLASS_TEMPLATE = """
package {package_name};

public class {class_name} {{

    private final long __id;

    {constructors}

    {methods}

    private Object __invoke(String signature, boolean isStatic, Object... args) {{
        long objectId = isStatic ? 0 : this.__id;
        return Bridge.invoke(objectId, "{full_class_name}", signature, args);
    }}
    
    private {class_name}(long id) {{
        this.__id = id;
    }}
}}
"""

CONSTRUCTOR_TEMPLATE = """
    public {class_name}({params}) {{
        this.__id = (long) Bridge.createObject("{full_class_name}", "{signature}"{args_comma}{args});
    }}
"""

METHOD_TEMPLATE = """
    public {static_modifier}{return_type} {method_name}({params}) {{
        {return_statement}__invoke("{signature}", {is_static}{args_comma}{args});
    }}
"""

def generate_signature(method_info, class_name):
    name = class_name if method_info['isConstructor'] else method_info['name']
    
    params = ",".join(method_info['paramTypes'])
    return f"{name}({params})"

def generate_java_params(param_types):
    params = []
    for i, p_type in enumerate(param_types):
        simple_type = p_type.split('<')[0]
        params.append(f"{simple_type} p{i}")
    
    return ", ".join(params)

def generate_args_list(param_count):
    if param_count == 0:
        return ""
    
    return ", ".join([f"p{i}" for i in range(param_count)])

def generate_args_comma(param_count):
    """Генерирует запятую для разделения аргументов (только если есть аргументы)"""
    if param_count == 0:
        return ""
    
    return ", "

def compile_java_file(file_path, classpath=None):
    try:
        file_dir = os.path.dirname(file_path)
        file_name = os.path.basename(file_path)
        
        cmd = ['javac']
        
        if classpath:
            cmd.extend(['-cp', classpath])
            
        cmd.append(file_name)
        
        result = subprocess.run(cmd, capture_output=True, text=True, cwd=file_dir)
        
        if result.returncode == 0:
            print(f"✓ Скомпилирован: {file_name}")
            
            return True
        else:
            print(f"✗ Ошибка компиляции {file_name}:")
            
            if result.stderr:
                print(result.stderr)
            
            return False
            
    except Exception as e:
        print(f"Исключение при компиляции {os.path.basename(file_path)}: {e}")
        return False

def generate_class_file(class_info, output_dir, compile_flag=False, classpath=None):
    full_name = class_info['name']
    
    if '$' in full_name:
        return False

    package_parts = full_name.split('.')
    package_name = ".".join(package_parts[:-1])
    class_name = package_parts[-1]

    package_path = os.path.join(output_dir, *package_name.split('.'))
    os.makedirs(package_path, exist_ok=True)

    constructors_code = []
    methods_code = []

    for method in class_info['methods']:
        signature = generate_signature(method, class_name)
        java_params_str = generate_java_params(method['paramTypes'])
        arg_names_str = generate_args_list(len(method['paramTypes']))
        args_comma_str = generate_args_comma(len(method['paramTypes']))

        if method['isConstructor']:
            code = CONSTRUCTOR_TEMPLATE.format(
                class_name=class_name,
                full_class_name=full_name,
                params=java_params_str,
                signature=signature,
                args_comma=args_comma_str,
                args=arg_names_str
            )

            constructors_code.append(code)
        else:
            is_static_bool_str = str(method['isStatic']).lower()
            static_modifier_str = "static " if method['isStatic'] else ""
            
            return_type = method['returnType'].split('<')[0]
            
            return_statement_str = ""
            if return_type != "void":
                return_statement_str = f"return ({return_type}) "

            code = METHOD_TEMPLATE.format(
                static_modifier=static_modifier_str,
                return_type=return_type,
                method_name=method['name'],
                params=java_params_str,
                return_statement=return_statement_str,
                signature=signature,
                is_static=is_static_bool_str,
                args_comma=args_comma_str,
                args=arg_names_str
            )

            methods_code.append(code)

    output_code = CLASS_TEMPLATE.format(
        package_name=package_name,
        class_name=class_name,
        full_class_name=full_name,
        constructors="\n".join(constructors_code),
        methods="\n".join(methods_code)
    )

    file_path = os.path.join(package_path, f"{class_name}.java")
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(output_code)
    
    print(f"Сгенерирован: {file_path}")
    
    if compile_flag:
        compile_success = compile_java_file(file_path, classpath)
        return compile_success
    
    return True

def main():
    if len(sys.argv) < 3:
        print("Использование: python generator.py путь_к_api.json папка_для_вывода [--compile] [--classpath путь]")
        print("Опции:")
        print("  --compile     Компилировать сгенерированные классы")
        print("  --classpath   Указать classpath для компиляции")
        sys.exit(1)

    json_path = sys.argv[1]
    output_dir = sys.argv[2]
    
    compile_flag = '--compile' in sys.argv
    classpath = None
    
    if '--classpath' in sys.argv:
        classpath_index = sys.argv.index('--classpath')
        if classpath_index + 1 < len(sys.argv):
            classpath = sys.argv[classpath_index + 1]

    with open(json_path, 'r', encoding='utf-8') as f:
        api_data = json.load(f)

    print(f"Загружено {len(api_data)} классов")
    
    success_count = 0
    error_count = 0
    
    for i, class_info in enumerate(api_data):
        print(f"\nОбработка класса {i+1}/{len(api_data)}...")
        
        result = generate_class_file(class_info, output_dir, compile_flag, classpath)
        if result is not False:
            if result:
                success_count += 1
            else:
                error_count += 1
    
    if compile_flag:
        print(f"Успешно сгенерировано и скомпилировано: {success_count}")
        print(f"С ошибками компиляции: {error_count}")
    else:
        print(f"Успешно сгенерировано: {success_count}")
        print(f"С ошибками генерации: {error_count}")
    
    print(f"Пропущено (вложенные классы): {len(api_data) - success_count - error_count}")
    print(f"Всего обработано: {success_count + error_count}")

if __name__ == "__main__":
    main()