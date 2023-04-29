import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    public static Logger logger;


    Logger(){}

    public static Logger getLogger(){
        if (logger == null){
            logger = new Logger();
        }
        return logger;
    }



    public void log(String msg) {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();

        String text = "[" + formatter.format(date) + "]" + msg;

        try(FileWriter writer = new FileWriter("file.log", true)) {
            writer.write(text);
            writer.append('\n');
            writer.flush();
        }
        catch(IOException ex){

            System.out.println(ex.getMessage());
        }
    }
}
