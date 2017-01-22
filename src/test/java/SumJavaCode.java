import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class SumJavaCode {
    private static long normalLines = 0; // 空行
    private static long commentLines = 0; // 注释行
    private static long whiteLines = 0; // 代码行

    public static void main(String[] args) {
        SumJavaCode sjc = new SumJavaCode();
        String path = args != null ? args[0] : "E:/codes-open-imp/TProfiler/src/main";
        File f = new File(path); //目录
        System.out.println(f.getName());
        sjc.countFile(f);
        System.out.println("空行：" + normalLines);
        System.out.println("注释行：" + commentLines);
        System.out.println("代码行：" + whiteLines);
    }

    /**
     * 查找出一个目录下所有的.java文件
     *
     * @param f 要查找的目录
     */
    private void countFile(File f) {
        File[] childs = f.listFiles();
        for (int i = 0; i < childs.length; i++) {
            if (!childs[i].isDirectory()) {
                if (childs[i].getName().endsWith(".java")) {
                    System.out.println(childs[i].getName());
                    sumCode(childs[i]);
                }
            } else {
                countFile(childs[i]);
            }
        }
    }

    /**
     * 计算一个.java文件中的代码行，空行，注释行
     *
     * @param file 要计算的.java文件
     */
    private void sumCode(File file) {
        BufferedReader br = null;
        boolean comment = false;
        try {
            br = new BufferedReader(new FileReader(file));
            String line = "";
            try {
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.matches("^[//s&&[^//n]]*$")) {
                        whiteLines++;
                    } else if (line.startsWith("/*") && !line.endsWith("*/")) {
                        commentLines++;
                        comment = true;
                    } else if (true == comment) {
                        commentLines++;
                        if (line.endsWith("*/")) {
                            comment = false;
                        }
                    } else if (line.startsWith("//")) {
                        commentLines++;
                    } else {
                        normalLines++;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                    br = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
