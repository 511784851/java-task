package com.blemobi.library.log;
/**
 * 
 * @author 李子才<davis.lee@blemobi.com>
 * 这是java的System.out输出流和System.err输出流重定向到Log4j的工具类。
 */

import java.io.PrintStream;

import lombok.extern.log4j.Log4j;

@Log4j
public class StdOutErrRedirect {  

    public static void redirectSystemOutAndErrToLog() {  
        PrintStream printStreamForOut = createLoggingWrapper(System.out, false);  
        PrintStream printStreamForErr = createLoggingWrapper(System.out, true);  
        System.setOut(printStreamForOut);  
        System.setErr(printStreamForErr);  
    }  
  
    private static PrintStream createLoggingWrapper(final PrintStream printStream, final boolean isErr) {  
        return new PrintStream(printStream) {  
            @Override  
            public void print(final String string) {  
                if (!isErr){  
                    log.debug(string);  
                }else{  
                	log.error(string);  
                }  
            }  
            @Override  
            public void print(boolean b) {  
                if (!isErr){  
                	log.debug(Boolean.valueOf(b));  
                }else{  
                	log.error(Boolean.valueOf(b));  
                }  
            }  
            @Override  
            public void print(char c) {  
                if (!isErr){  
                	log.debug(Character.valueOf(c));  
                }else{  
                	log.error(Character.valueOf(c));  
                }  
            }  
            @Override  
            public void print(int i) {  
                if (!isErr){  
                	log.debug(String.valueOf(i));  
                }else{  
                	log.error(String.valueOf(i));  
                }  
            }  
            @Override  
            public void print(long l) {  
                if (!isErr){  
                	log.debug(String.valueOf(l));  
                }else{  
                	log.error(String.valueOf(l));  
                }  
            }  
            @Override  
            public void print(float f) {  
                if (!isErr){  
                	log.debug(String.valueOf(f));  
                }else{  
                	log.error(String.valueOf(f));  
                }  
            }  
            @Override  
            public void print(double d) {  
                if (!isErr){  
                	log.debug(String.valueOf(d));  
                }else{  
                	log.error(String.valueOf(d));  
                }  
            }  
            @Override  
            public void print(char[] x) {  
                if (!isErr){  
                	log.debug(x == null ? null : new String(x));  
                }else{  
                	log.error(x == null ? null : new String(x));  
                }  
            }  
            @Override  
            public void print(Object obj) {  
                if (!isErr){  
                	log.debug(obj);  
                }else{  
                	log.error(obj);  
                }  
            }  
        };  
    }  
}  