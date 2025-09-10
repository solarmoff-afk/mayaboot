import com.google.gson.GsonBuilder;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.MethodParameterInfo;
import io.github.classgraph.ScanResult;

import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

public class ApiExtractor {
    public static class ExtractedMethodInfo {
        public String name;
        public String returnType;
        public List<String> paramTypes = new ArrayList<>();
        public boolean isStatic;
        public boolean isConstructor;
    }

    public static class ExtractedClassInfo {
        public String name;
        public String superclass;
        public List<String> interfaces = new ArrayList<>();
        public List<ExtractedMethodInfo> methods = new ArrayList<>();
    }

    public static void main(String[] args) throws IOException {
        String androidJarPath = "android.jar";
        List<ExtractedClassInfo> allClassInfos = new ArrayList<>();

        try (ScanResult scanResult = new ClassGraph()
                .overrideClasspath(androidJarPath)
                .enableAllInfo()
                .acceptPackages("android", "androidx", "com.google.android")
                .scan()) {
            
            for (io.github.classgraph.ClassInfo cgClassInfo : scanResult.getAllClasses()) {
                if (!cgClassInfo.isPublic())
                    continue;

                ExtractedClassInfo ci = new ExtractedClassInfo();
                ci.name = cgClassInfo.getName();

                if (cgClassInfo.getSuperclass() != null) {
                    ci.superclass = cgClassInfo.getSuperclass().getName();
                }
                
                ci.interfaces.addAll(cgClassInfo.getInterfaces().getNames());

                for (MethodInfo constructorInfo : cgClassInfo.getConstructorInfo()) {
                    if (!constructorInfo.isPublic())
                        continue;
                    
                    ExtractedMethodInfo method = new ExtractedMethodInfo();
                    method.name = "<init>";
                    method.isConstructor = true;

                    for (MethodParameterInfo pi : constructorInfo.getParameterInfo()) {
                        method.paramTypes.add(pi.getTypeSignatureOrTypeDescriptor().toString());
                    }

                    ci.methods.add(method);
                }

                for (MethodInfo methodInfo : cgClassInfo.getMethodInfo()) {
                    if (!methodInfo.isPublic() || methodInfo.isBridge())
                        continue;
                    
                    ExtractedMethodInfo method = new ExtractedMethodInfo();
                    method.name = methodInfo.getName();
                    method.isStatic = methodInfo.isStatic();
                    method.returnType = methodInfo.getTypeDescriptor().getResultType().toString();
                    // method.returnType = methodInfo.getTypeSignatureOrTypeDescriptor().getReturnType().toString();
                    
                    for (MethodParameterInfo pi : methodInfo.getParameterInfo()) {
                        method.paramTypes.add(pi.getTypeSignatureOrTypeDescriptor().toString());
                    }

                    ci.methods.add(method);
                }
                allClassInfos.add(ci);
            }
        }

        try (FileWriter writer = new FileWriter("api.json")) {
            new GsonBuilder().setPrettyPrinting().create().toJson(allClassInfos, writer);
        }
 
        System.out.println("API dumped to api.json successfully!");
    }
}