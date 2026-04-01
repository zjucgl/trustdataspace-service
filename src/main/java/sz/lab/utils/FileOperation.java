package sz.lab.utils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class FileOperation {

    /**
     * 创建文件
     * @return
     */
    public static boolean createFile(File file) {
        boolean flag = false;
        try {
            if (!file.exists()) {
                file.createNewFile();
                flag = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 读取文件内容
     * @param filePathAndName String 如 c:\\1.txt 绝对路径
     * @return boolean
     */
    public static String readFile(String filePathAndName) {
        String result = "";
        StringBuffer sb = new StringBuffer();
        FileInputStream fis = null;
        InputStreamReader read = null;
        BufferedReader reader = null;
        try {
            File file = new File(filePathAndName);
            if (file.isFile() && file.exists()) {
                fis = new FileInputStream(file);
                read = new InputStreamReader(fis, "UTF-8");
                reader = new BufferedReader(read);
                String line = "";
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                result = sb.toString();
                sb.setLength(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                    reader = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (read != null) {
                    read.close();
                    read = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fis != null) {
                    fis.close();
                    fis = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 读SQL文件内容
     * @return
     */
    public static String readSQLFile(File file) {
        String result = "";
        StringBuffer sb = new StringBuffer();
        FileInputStream fis = null;
        InputStreamReader read = null;
        BufferedReader reader = null;
        try {
            if (file != null ) {
                System.out.println(1);
            }else{
                System.out.println("1非");
            }
            if (file.isFile() ) {
                System.out.println(2);
            }else{
                System.out.println("2非");
            }
            if (file.exists()) {
                System.out.println(3);
            }else{
                System.out.println("3非");
            }

            if (file != null && file.isFile() && file.exists()) {
                fis = new FileInputStream(file);
                read = new InputStreamReader(fis, "UTF-8");
                reader = new BufferedReader(read);
                String line;
                boolean ck = false;
                while ((line = reader.readLine()) != null) {
                    if(ck){//第一行忽略
                        if (line != null && !line.toString().contains("--")) {//没注释的
                            sb.append(line + " ");
                        }else if (line != null && line.toString().contains("--")) {//有注释的
                            if(line.split("--").length>0 && line.split("--")[0].trim().length()>1){
                                sb.append(line.split("--")[0] + " ");
                            }
                        }
                    }
                    ck = true;
                }
                result = sb.toString();
                sb.setLength(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                    reader = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (read != null) {
                    read.close();
                    read = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fis != null) {
                    fis.close();
                    fis = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 读TXT文件内容
     * @return
     */
    public static String readTxtFile(File file) {
        String result = "";
        StringBuffer sb = new StringBuffer();
        InputStreamReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            if (file!=null && file.isFile() && file.exists()) {
                //fileReader = new FileReader(file);
                fileReader = new InputStreamReader(new FileInputStream(file), "UTF-8");
                bufferedReader = new BufferedReader(fileReader);
                String read = null;
                while ((read = bufferedReader.readLine()) != null) {
                    sb.append(read);
                }
                result = sb.toString();
                sb.setLength(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                    bufferedReader = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fileReader != null) {
                    fileReader.close();
                    fileReader = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 写入文件
     * @param content
     * @throws Exception
     */
    public static boolean writeTxtFile(String content, File file) {
        boolean flag = false;
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        BufferedWriter writer = null;
        try {
            fos = new FileOutputStream(file);
            osw = new OutputStreamWriter(fos, "UTF-8");
            writer = new BufferedWriter(osw);
            writer.write(content);
            writer.flush();
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (osw != null) {
                    osw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

    /**
     * 重新写文件
     * @param filePath
     * @param content
     */
    public static void contentToTxt(String filePath, String content) {
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        BufferedWriter writer = null;
        try {
            File file = new File(filePath);
            if (!(file.isFile() && file.exists())) {
                file.createNewFile();// 文件不存在则创建
            }
            fos = new FileOutputStream(file);
            osw = new OutputStreamWriter(fos, "UTF-8");
            writer = new BufferedWriter(osw);
            writer.write(content);
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (osw != null) {
                    osw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

