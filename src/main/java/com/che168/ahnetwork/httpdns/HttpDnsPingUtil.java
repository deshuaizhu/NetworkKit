package com.che168.ahnetwork.httpdns;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gejinpeng on 2015/11/11.
 */
public class HttpDnsPingUtil {
    private static final String COMMAND_SH = "sh";
    private static final String COMMAND_LINE_END = "\n";
    private static final String COMMAND_EXIT = "exit\n";

    private static final String PING = "PING";
    private static final String TIME_PING = "time=";
    private static final String PARENTHESE_OPEN_PING = "(";
    private static final String PARENTHESE_CLOSE_PING = ")";

    private static final int MSG_PING = 10010;

    private static Context mContext;
    private static PingListener mListener;

    private static String mHost;
    /**
     * 执行单条命令
     *
     * @param command
     * @return
     */
    public static String execute(String command) {
        return execute(new String[]{command});
    }

    /**
     * 可执行多行命令（bat）
     *
     * @param commands
     * @return
     */
    public static String execute(String[] commands) {
        List<String> results = new ArrayList<String>();
        StringBuffer stringBuffer = new StringBuffer("");
        int status = -1;
        if (commands == null || commands.length == 0) {
            return null;
        }
        Process process = null;
        BufferedReader successReader = null;
        BufferedReader errorReader = null;
        StringBuilder errorMsg = null;

        DataOutputStream dos = null;
        try {
            process = Runtime.getRuntime().exec(COMMAND_SH);
            dos = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) {
                    continue;
                }
                dos.write(command.getBytes());
                dos.writeBytes(COMMAND_LINE_END);
                dos.flush();
            }
            dos.writeBytes(COMMAND_EXIT);
            dos.flush();

            status = process.waitFor();

            errorMsg = new StringBuilder();
            successReader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            errorReader = new BufferedReader(new InputStreamReader(
                    process.getErrorStream()));
            String lineStr;
            while ((lineStr = successReader.readLine()) != null) {
                results.add(lineStr);
                stringBuffer.append(lineStr + "\n");
            }
            while ((lineStr = errorReader.readLine()) != null) {
                errorMsg.append(lineStr);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (dos != null) {
                    dos.close();
                }
                if (successReader != null) {
                    successReader.close();
                }
                if (errorReader != null) {
                    errorReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (process != null) {
                process.destroy();
            }
        }
        return stringBuffer.toString();
    }

    public static void startPing(Context context,String host, List<String> urls, final PingListener listener) {
        mContext = context;
        mListener = listener;
        mHost = host;
        Message message = Message.obtain();
        message.what = MSG_PING;
        message.obj = urls;
        handler.sendMessage(message);
    }

    public static void stopPing() {
        handler.removeMessages(MSG_PING);
    }

    private static Handler handler = new PingHandler();

    private static final class PingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_PING && msg.obj != null && msg.obj instanceof List) {
                pingRunnable = new PingRunnable((List<String>) msg.obj);
                pingRunnable.setContext(mContext);
                pingRunnable.setListener(mListener);
                new Thread(pingRunnable).start();
            }
        }
    }

    /**
     * 从结果集中解析出ip
     *
     * @param ping
     * @return
     */
    private static String parseIP(String ping) {
        String ip = "";
        if (ping.contains(PING)) {
            int indexOpen = ping.indexOf(PARENTHESE_OPEN_PING);
            int indexClose = ping.indexOf(PARENTHESE_CLOSE_PING);

            ip = ping.substring(indexOpen + 1, indexClose);
        }

        return ip;
    }

    /**
     * 从结果集中解析出time
     *
     * @param ping
     * @return
     */
    private static double parseTime(String ping) {
        String time = "";
        if (ping.contains(TIME_PING)) {
            int index = ping.indexOf(TIME_PING);

            time = ping.substring(index + 5);
            index = time.indexOf(" ");
            time = time.substring(0, index);
        }
        if(TextUtils.isEmpty(time)){
            return 0;
        }
        try {
            return Double.parseDouble(time);
        }catch (NumberFormatException ex){
            return 0;
        }
    }

    public interface PingListener {
        void onFinished(String host, String ip);
    }

    private static PingRunnable pingRunnable = null;

    private static class PingRunnable implements Runnable {
        private Context mContext;
        private PingListener mListener;

        private List<String> mURLs;

        public PingRunnable(List<String> urls) {
            this.mURLs = urls;
        }

        private boolean mRunning;

        public void setContext(Context context) {
            mContext = context;
        }

        public void setListener(PingListener listener) {
            mListener = listener;
        }

        @Override
        public void run() {
            if (mContext == null) return;

            if (mContext instanceof Activity && ((Activity) mContext).isFinishing()) {
                mRunning = false;
                return;
            }

            if (mRunning) return;

            mRunning = true;

            String ip = null;
            double time = 0;
            for (final String url : mURLs) {
                try {
                    int timeOut = 3000; // 超时应该在3钞以上
                    boolean status = false;
                    try {
                        status = InetAddress.getByName(url).isReachable(timeOut);
                    } catch (UnknownHostException e) {
                        status = false;
                    } catch (IOException e) {
                        status = false;
                    }

                    if (!status) {
                        continue;
                    }

                    String command = "ping -c 1 -t 30 ";

                    String res = execute(command + url);

                    // 第一次调用ping命令的时候 记得把取得的最终的ip地址 赋给外面的ipToPing
                    // 后面要依据这个ipToPing的值来判断是否到达ip数据报的 终点
                    String resIp = parseIP(res);
                    Double resTime = parseTime(res);
                    System.out.println("hcp-->host:"+mHost+"-ip:"+resIp+"_time:"+resTime);
                    if(time == 0){
                        time = resTime;
                        ip = resIp;
                    }
                    if(resTime < time){
                        time = resTime;
                        ip = resIp;
                    }
                } catch (Exception e) {
                    mRunning = false;
                }
            }
            if (mListener != null) {
                mListener.onFinished(mHost,ip);
            }
            mRunning = false;
        }
    }

}
