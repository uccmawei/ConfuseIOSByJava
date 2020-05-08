import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by UCCMAWEI on 2020/04/28.
 * <p>
 * 遍历工程，实现替换类名和方法名
 */

public class Main {

    public static void main(String[] args) {
        start();
    }

    // Manager 目录
    private static String PROJECT_PATH = "/Users/mac/Desktop/Project/Manager";

    // 混淆方法标记
    private static String CONFUSED_METHOD_TAG = "// CONFUSED_METHOD_TAG";

    // 忽略目录
    private static String[] IGNORE_CLASS_PATH_ARRAY = {"Manager/Manager/Lib", "Manager/Manager/Utils"};

    // 忽略目录
    private static String[] IGNORE_METHOD_PATH_ARRAY = {"Manager/Manager/Lib", "Manager/Manager/UI"};

    // 需要混淆的类名，混淆后的类名
    private static List<String> targetClassNameList;
    private static List<String> newClassNameList;

    // 方法名  属性名
    private static HashSet<String> targetMethodNameSet;
    private static List<String> targetMethodNameList;
    private static List<String> newMethodNameList;

    // 开始
    public static void start() {
        try {
            targetClassNameList = new ArrayList<>();
            newClassNameList = new ArrayList<>();
            targetMethodNameSet = new HashSet<>();
            targetMethodNameList = new ArrayList<>();
            newMethodNameList = new ArrayList<>();

            File file1 = new File(PROJECT_PATH + "/Manager");
            File file2 = new File(PROJECT_PATH + "/Manager.xcodeproj");

            // 遍历第一次，找到需要混淆的类名
            checkFileByFolder(file1);

            // 处理带加号的
            handleNameWithPlus();

            // 找出所有方法名
            findAllMethodName(file1);

            // 再遍历一次，修改文件内容
            editAllFile(file1);

            // 最后遍历一次，把文件后缀改回来
            fixFileName(file1);

            // 再遍历一次，修改文件内容
            editAllFile(file2);

            // 最后遍历一次，把文件后缀改回来
            fixFileName(file2);

            // 结束
            System.out.println("FUCK DONE!!!");

            // 打印所有的方法名
            System.out.println(Arrays.toString(targetMethodNameList.toArray()));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("FUCK FUCK FUCK FUCK !!! 报错了");
        }
    }

    // 1、根据文件夹找出需要混淆的类
    private static void checkFileByFolder(File file) {
        if (file.isDirectory()) {
            for (int i = 0; i < IGNORE_CLASS_PATH_ARRAY.length; i++) {
                if (file.getAbsolutePath().contains(IGNORE_CLASS_PATH_ARRAY[i])) {
                    return;
                }
            }

            File[] list = file.listFiles();
            for (int i = 0; i < list.length; i++) {
                checkFileByFolder(list[i]);
            }
            return;
        }

        // 只针对h文件
        if (!file.getName().endsWith(".h")) {
            return;
        }

        targetClassNameList.add(file.getName().substring(0, file.getName().length() - 2));
        newClassNameList.add("NCLN" + targetClassNameList.size());
    }

    // 2、处理带加号的
    private static void handleNameWithPlus() {
        for (int i = 0; i < targetClassNameList.size(); i++) {
            if (targetClassNameList.get(i).contains("+")) {
                String firstName = targetClassNameList.get(i).split("\\+")[0];
                String newFirstName = firstName;
                for (int i1 = 0; i1 < targetClassNameList.size(); i1++) {
                    if (firstName.equals(targetClassNameList.get(i1))) {
                        newFirstName = newClassNameList.get(i1);
                        break;
                    }
                }
                newClassNameList.set(i, newFirstName + "+" + newClassNameList.get(i));
            }
        }
    }

    // 3、找出所有方法名字
    private static void findAllMethodName(File file) throws Exception {
        if (file.isDirectory()) {
            for (int i = 0; i < IGNORE_CLASS_PATH_ARRAY.length; i++) {
                if (file.getAbsolutePath().contains(IGNORE_CLASS_PATH_ARRAY[i])) {
                    return;
                }
            }

            File[] list = file.listFiles();
            for (int i = 0; i < list.length; i++) {
                findAllMethodName(list[i]);
            }
            return;
        }

        if (!file.getName().endsWith(".m")) {
            return;
        }

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

        boolean isTagForPrevious = false;
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {

            // 上一行是标记就意味着这一行是方法名
            if (isTagForPrevious) {

                // 寻找方法签名
                Matcher matcher = Pattern.compile("^\\s*[-+]\\s*\\([\\w<>\\s\\*]+\\)\\s*(\\b\\w+\\b)").matcher(line);
                if (matcher.find()) {
                    String key = matcher.group(1);
                    if (!targetMethodNameSet.contains(key)) {
                        targetMethodNameSet.add(key);
                        targetMethodNameList.add(key);
                        newMethodNameList.add("nmtm" + targetMethodNameList.size());
                    }
                }
                isTagForPrevious = false;
            } else {
                // 混淆TAG的话就不用在处理了，不把内容写进去
                if (line.equals(CONFUSED_METHOD_TAG)) {
                    isTagForPrevious = true;
                    continue;
                }
            }
        }

        bufferedReader.close();
    }

    // 4、遍历修改文件内容
    private static void editAllFile(File file) throws Exception {
        if (file.isDirectory()) {
            File[] list = file.listFiles();
            for (int i = 0; i < list.length; i++) {
                editAllFile(list[i]);
            }
            return;
        }

        // 只支持这几种类型
        if (!Pattern.compile("^[\\w\\+-]+\\.(xib|h|m|pch|pbxproj)$").matcher(file.getName()).find()) {
            return;
        }

        // 输出目标路径
        String outputFilePath = file.getAbsolutePath() + ".edit";
        System.out.println("原路径：" + outputFilePath);

        for (int i = 0; i < targetClassNameList.size(); i++) {
            if (Pattern.compile("^" + Pattern.quote(targetClassNameList.get(i)) + "\\.(xib|h|m|pch|pbxproj)$").matcher(file.getName()).find()) {
                int lastIndex = outputFilePath.indexOf(file.getName());
                String suffixName = file.getName().substring(file.getName().indexOf("."));
                outputFilePath = outputFilePath.substring(0, lastIndex) + newClassNameList.get(i) + suffixName;
                break;
            }
        }

        System.out.println("新路径：" + outputFilePath);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        FileWriter fileWriter = new FileWriter(outputFilePath);

        String line = null;
        boolean printMethod = false;
        while ((line = bufferedReader.readLine()) != null) {

            String oldLine = line;

            // 替换类名
            for (int i = 0; i < targetClassNameList.size(); i++) {
                if (line.contains(targetClassNameList.get(i))) {
                    if (line.contains("/* " + targetClassNameList.get(i) + " */")) {
                        continue;
                    }
                    if (line.contains("path = " + targetClassNameList.get(i) + ";")) {
                        continue;
                    }
                    line = line.replaceAll(Pattern.quote(" " + targetClassNameList.get(i)) + "$", " " + newClassNameList.get(i));
                    line = line.replaceAll(Pattern.quote("#import \"" + targetClassNameList.get(i) + ".h\""), "#import \"" + newClassNameList.get(i) + ".h\"");
                    line = line.replaceAll(Pattern.quote(" " + targetClassNameList.get(i) + " "), " " + newClassNameList.get(i) + " ");
                    line = line.replaceAll("^" + Pattern.quote(targetClassNameList.get(i) + " "), " " + newClassNameList.get(i) + " ");
                    line = line.replaceAll(Pattern.quote(" " + targetClassNameList.get(i) + "("), " " + newClassNameList.get(i) + "(");
                    line = line.replaceAll("^" + Pattern.quote(targetClassNameList.get(i) + "("), " " + newClassNameList.get(i) + "(");
                    line = line.replaceAll(Pattern.quote(" " + targetClassNameList.get(i) + "."), " " + newClassNameList.get(i) + ".");
                    line = line.replaceAll("^" + Pattern.quote(targetClassNameList.get(i) + "."), " " + newClassNameList.get(i) + ".");
                    line = line.replaceAll(Pattern.quote("\"" + targetClassNameList.get(i) + "."), "\"" + newClassNameList.get(i) + ".");
                    line = line.replaceAll(Pattern.quote(" " + targetClassNameList.get(i) + "<"), " " + newClassNameList.get(i) + "<");
                    line = line.replaceAll("^" + Pattern.quote(targetClassNameList.get(i) + "<"), " " + newClassNameList.get(i) + "<");
                    line = line.replaceAll(Pattern.quote(" " + targetClassNameList.get(i) + ","), " " + newClassNameList.get(i) + ",");
                    line = line.replaceAll("^" + Pattern.quote(targetClassNameList.get(i) + ","), " " + newClassNameList.get(i) + ",");
                    line = line.replaceAll(Pattern.quote(" " + targetClassNameList.get(i) + ";"), " " + newClassNameList.get(i) + ";");
                    line = line.replaceAll("^" + Pattern.quote(targetClassNameList.get(i) + ";"), " " + newClassNameList.get(i) + ";");
                    line = line.replaceAll(Pattern.quote(" " + targetClassNameList.get(i) + "*"), " " + newClassNameList.get(i) + "*");
                    line = line.replaceAll("^" + Pattern.quote(targetClassNameList.get(i) + "*"), " " + newClassNameList.get(i) + "*");
                    line = line.replaceAll(Pattern.quote("," + targetClassNameList.get(i) + ";"), "," + newClassNameList.get(i) + ";");
                    line = line.replaceAll(Pattern.quote("," + targetClassNameList.get(i) + " "), "," + newClassNameList.get(i) + " ");
                    line = line.replaceAll(Pattern.quote("," + targetClassNameList.get(i) + "*"), "," + newClassNameList.get(i) + "*");
                    line = line.replaceAll(Pattern.quote("[" + targetClassNameList.get(i) + " "), "[" + newClassNameList.get(i) + " ");
                    line = line.replaceAll(Pattern.quote("(" + targetClassNameList.get(i) + " "), "(" + newClassNameList.get(i) + " ");
                    line = line.replaceAll(Pattern.quote(")" + targetClassNameList.get(i) + " "), ")" + newClassNameList.get(i) + " ");
                    line = line.replaceAll(Pattern.quote(")" + targetClassNameList.get(i) + "*"), ")" + newClassNameList.get(i) + "*");
                    line = line.replaceAll(Pattern.quote("(" + targetClassNameList.get(i) + "*"), "(" + newClassNameList.get(i) + "*");
                    line = line.replaceAll(Pattern.quote("(" + targetClassNameList.get(i) + "<"), "(" + newClassNameList.get(i) + "<");
                    line = line.replaceAll(Pattern.quote("@\"" + targetClassNameList.get(i) + "\""), "@\"" + newClassNameList.get(i) + "\"");
                    line = line.replaceAll(Pattern.quote("\"" + targetClassNameList.get(i) + "\""), "\"" + newClassNameList.get(i) + "\"");
                    line = line.replaceAll("^" + Pattern.quote("\"" + targetClassNameList.get(i) + "\""), "\"" + newClassNameList.get(i) + "\"");
                    line = line.replaceAll(Pattern.quote("/" + targetClassNameList.get(i) + "\""), "/" + newClassNameList.get(i) + "\"");
                    line = line.replaceAll(Pattern.quote("/" + targetClassNameList.get(i) + "."), "/" + newClassNameList.get(i) + ".");
                    line = line.replaceAll(Pattern.quote("<" + targetClassNameList.get(i) + ">"), "<" + newClassNameList.get(i) + ">");
                    line = line.replaceAll(Pattern.quote("<" + targetClassNameList.get(i) + ">"), "<" + newClassNameList.get(i) + ">");
                    line = line.replaceAll(Pattern.quote("<" + targetClassNameList.get(i) + " "), "<" + newClassNameList.get(i) + " ");
                    line = line.replaceAll(Pattern.quote("<" + targetClassNameList.get(i) + "*"), "<" + newClassNameList.get(i) + "*");
                }
            }

            // 不处理忽略文件夹里的方法替换
            boolean ignoreMethod = false;
            for (int i = 0; i < IGNORE_METHOD_PATH_ARRAY.length; i++) {
                if (file.getAbsolutePath().contains(IGNORE_METHOD_PATH_ARRAY[i])) {
                    ignoreMethod = true;
                    break;
                }
            }
            // 替换方法名字
            if (!ignoreMethod) {
                for (int i = 0; i < targetMethodNameList.size(); i++) {
                    if (line.contains(targetMethodNameList.get(i))) {
                        String targetName = targetMethodNameList.get(i);
                        String newName = newMethodNameList.get(i);
                        printMethod = true;

                        line = replaceMethodSignature1(line, targetName, newName);
                        line = replaceMethodCall1(line, targetName, newName);
                        line = replaceMethodCall2(line, targetName, newName);
                        line = replaceMethodCall3(line, targetName, newName);
                    }
                }

                if (printMethod) {
                    System.out.println("方法名换之前：" + oldLine);
                    System.out.println("方法名换之后：" + line);
                    printMethod = false;
                }
            }

            fileWriter.write(line + "\n");
        }

        bufferedReader.close();
        fileWriter.close();

        file.delete();
    }

    // 5、修正改动后的文件名
    private static void fixFileName(File file) throws Exception {
        if (file.isDirectory()) {
            File[] list = file.listFiles();
            for (int i = 0; i < list.length; i++) {
                fixFileName(list[i]);
            }
            return;
        }

        if (file.getName().endsWith(".edit")) {
            file.renameTo(new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - 5)));
        }
    }

    // 替换方法调用1
    private static String replaceMethodSignature1(String line, String targetName, String newName) {
        Matcher matcher = Pattern.compile("^\\s*[-+]\\s*\\(([\\w<>\\s\\*]+)\\)\\s*(\\b" + targetName + "\\b)").matcher(line);
        if (matcher.find()) {
            line = line.substring(0, matcher.start(2)) + newName + line.substring(matcher.start(2) + targetName.length());
        }

        return line;
    }

    // 替换方法调用1
    private static String replaceMethodCall1(String line, String targetName, String newName) {
        Matcher matcher = Pattern.compile("\\[.*[\\s\\]]+(\\b" + targetName + "\\b)").matcher(line);
        if (matcher.find()) {
            line = line.substring(0, matcher.start(1)) + newName + line.substring(matcher.start(1) + targetName.length());
            return replaceMethodCall1(line, targetName, newName);
        }

        return line;
    }

    // 替换方法调用2
    private static String replaceMethodCall2(String line, String targetName, String newName) {
        Matcher matcher = Pattern.compile(".*\\@selector\\s*\\(\\s*(\\b" + targetName + "\\b)").matcher(line);
        if (matcher.find()) {
            line = line.substring(0, matcher.start(1)) + newName + line.substring(matcher.start(1) + targetName.length());
            return replaceMethodCall2(line, targetName, newName);
        }

        return line;
    }

    // 替换方法调用3
    private static String replaceMethodCall3(String line, String targetName, String newName) {
        line = line.replaceAll("<action selector=\"" + targetName + ":\"", "<action selector=\"" + newName + ":\"");
        return line;
    }
}
