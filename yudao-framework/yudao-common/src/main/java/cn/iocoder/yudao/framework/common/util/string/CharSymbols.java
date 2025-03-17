package cn.iocoder.yudao.framework.common.util.string;

/**
 * 常用字符
 *  */
public class CharSymbols {

    /**
     * 空白
     * */
    public static final String EMPTY="";

    /**
     * 空白
     * */
    public static final String EQ="=";

    /**
     * 空白
     * */
    public static final String NU="#";

    /**
     * 逗号
     * */
    public static final String COMMA=",";

    /**
     * @
     * */
    public static final String AT="@";

    /**
     * @
     * */
    public static final String AND="&";

    /**
     * ?
     * */
    public static final String QUESTION="?";

    /**
     * 星号
     * */
    public static final String STAR="*";

    /**
     * 小数点
     * */
    public static final String DOT=".";

    /**
     * 小数点，正则
     * */
    public static final String DOT_RE="\\.";



    /**
     * 冒号
     * */
    public static final String COLON=":";

    /**
     * 竖线
     * */
    public static final String VERTICAL_LINE="|";

    /**
     * 下划线
     * */
    public static final String UNDER_LINE="_";

    /**
     * 减号
     * */
    public static final String MINUS="-";

    /**
     * 加号
     * */
    public static final String PLUS="+";

    /**
     * 代字号
     * */
    public static final String TILDE="~";


    /**
     * 斜杠
     * */
    public static final String SLASH="/";


    /**
     * 反斜杠
     * */
    public static final String BACK_SLASH="\\";

    /**
     * 换行 line feed
     * */
    public static final String LF="\n";


    public static final String LF_RE = "\\n";

    /**
     * 回车 carriage return
     * */
    public static final String CR="\r";

    /**
     * TAB
     * */
    public static final String TAB="\t";


    /**
     * null
     * */
    public static final String NULL="null";

    public static final String SPACE = " ";
    public static final String LEFT_BRACES = "{";
    public static final String PERCENT = "%";

    public static final String EMPTY_COLLECTION = "∅";

    public static String repeat(String c,int len) {
        StringBuilder r=new StringBuilder();
        while (r.length()<len) {
            r.append(c);
        }
        return r.toString();
    }
}
