import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.*;
import java.util.*;


public class PaJson {
    public static String getContentFromUrl(String myUrl, String charset) {
        StringBuffer sb = new StringBuffer();
        URL url;
        try {
            url = new URL(myUrl);
            URLConnection conn = url.openConnection();
            InputStream is = conn.getInputStream();
            Scanner sc = new Scanner(is, charset);
            while (sc.hasNextLine()) {
                sb.append(sc.nextLine()).append("\r\n");
            }
            sc.close();
            is.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("Read from Successfully");
        return sb.toString();
    }

    public static void writeFile(String str){
        try {
            PrintWriter out=new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream("summary.txt"))));
            out.println(str);
            out.close();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void insertsql(String sql)
    {
        String driverName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        String dbURL = "jdbc:sqlserver://XXXXXXXX;DatabaseName=XXXXXXX";
        String userName = "XXXXXXX";
        String userPwd = "XXXXXXXXXX";
        Connection dbConn = null;
        try
        {
            Class.forName(driverName);
            dbConn = DriverManager.getConnection(dbURL, userName, userPwd);
            //System.out.println("successfully connected");
            Statement stat = dbConn.createStatement();
            stat.execute(sql);
            //System.out.println("successfully insert");
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
            //System.out.print("false");
        }
    }

    public static String readdate(String sql) {
        String user = "XXXXXXX";
        String password = "XXXXXXXXXX";
        Connection conn;
        Statement stmt;
        ResultSet rs;

        String url = "jdbc:sqlserver://XXXXXXX;DatabaseName=XXXXXX";
        try {
            // 连接数据库
            conn = DriverManager.getConnection(url, user, password);
            // 建立Statement对象
            stmt = conn.createStatement();
            // 执行数据库查询语句
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String date = rs.getString("title");

                //System.out.println(date);
                return date;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            //System.out.println("数据库连接失败");
        }
        return null;
    }

    public static  void MyThread(){
        new Thread(){
            public void run(){
                boolean flg = false;
                while(!flg){
                    try{
                        String context = getContentFromUrl("https://XXXXXXXXXXXXXXXXjson","utf-8");
                        String tag = "opportunity(.*?)/opportunity";
                        String aname = "\"title\":(.*?),";
                        String atime = "\"updatetime\": \"(.*?)\"";

                        String limitmainblock = "主题板块：(.*?)\\\\";
                        String limitsigle = "影响个股：(.*?)\\\\";

                        List<String> namelist= DealStrSub.getSubUtil(context,aname);
                        List<String> pieces = DealStrSub.getSubUtil(context,tag);
                        List<String> timelist=DealStrSub.getSubUtil(context,atime);
                        List<String> diagram=new ArrayList<String>();

                        String regex = "([^(0-9\\u4e00-\\u9fa5)])";
                        String finallist=null;

                        int newtitleztbksize = 0;

                        String nowtitle=namelist.get(0);
                        String standardtitle = readdate("select Top 1 title from temp_t order by [date] desc ");

                        Boolean check=false;

                        ListIterator<String> listIterator = pieces.listIterator();

                        while(listIterator.hasNext()){

                            String next = listIterator.next();

                            if(check==false){
                                check=true;
                            }else {
                                check=false;
                                diagram.add(next);
                            }
                        }

                        List<String> grid = new ArrayList<String>();
                        for(int i=0;i<diagram.size();i++){
                            String sentence = namelist.get(i) +"@"+timelist.get(i);

                            List<String> mainblock = DealStrSub.getSubUtil(diagram.get(i),limitmainblock);
                            List<String> single = DealStrSub.getSubUtil(diagram.get(i),limitsigle);

                            for (int j=0;j<mainblock.size();j++){
                                /*System.out.println(j);*/
                                if(j==0){
                                    newtitleztbksize= mainblock.size();
                                }
                                String sigleline=null;
                                if(j<single.size()){
                                    sigleline = single.get(j);
                                }else {
                                    sigleline="null";
                                }
                                String line=sentence+"@"+mainblock.get(j)+"@"+sigleline;
                                grid.add(line);
                            }
                        }
                        //全录入
                        /*        for (int k=0;k<grid.size();k++){
                                    String line= grid.get(k);
                                    if(finallist==null){
                                        finallist=line;
                                    }else{
                                        finallist=finallist+"\n"+line;
                                    }
                                    String[] needin = line.split("@");
                                    String sql = "insert into  temp_t values('"+needin[0]+"', '"+needin[1]+"','"+needin[2]+"','"+needin[3]+"')";
                                    insertsql(sql);
                        }*/

                        //最新录入
                        if(!nowtitle.equals(standardtitle)){
                            for (int i=0;i<newtitleztbksize;i++){
                                String line=grid.get(i);
                                String[] needin = line.split("@");
                                String sql = "insert into  temp_t values('"+needin[0]+"', '"+needin[1]+"','"+needin[2]+"','"+needin[3]+"')";
                                insertsql(sql);
                            }
                            standardtitle=nowtitle;
                            System.out.println("Update new");
                        }
                        Thread.sleep(1800000);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }.start();
    }

    public static void main(String[] args) {
        MyThread();
    }
}
