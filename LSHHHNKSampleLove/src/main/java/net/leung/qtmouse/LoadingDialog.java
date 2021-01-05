package net.leung.qtmouse;

import android.app.Dialog;
import android.content.Context;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

/**
 * Created by tjy on 2017/6/19.
 */
public class LoadingDialog extends Dialog{


    public Context context;
    public  String message="";
    public  boolean isShowMessage=true;
    public  boolean isCancelable=false;
    public  boolean isCancelOutside=false;
    public   TextView msgText;
    private static LoadingDialog loadingDialog;

    public void setInitText(String initText) {
        this.initText = initText;
    }

    String  initText="添加设备中";
    public LoadingDialog(@NonNull Context context, String message, boolean isShowMessage, boolean isCancelable, boolean isCancelOutside) {

        super(context,R.style.MyDialogStyle);
        this.context = context;
        this.message = message;
        this.isShowMessage = isShowMessage;
        this.isCancelable = isCancelable;
        this.isCancelOutside = isCancelOutside;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view=inflater.inflate(R.layout.dialog_loading_wait,null);

        msgText = (TextView) view.findViewById(R.id.tipTextView);
        if(isShowMessage){
            msgText.setText(message);
        }else{
            msgText.setVisibility(View.GONE);
        }
        setContentView(view);
        setCancelable(isCancelable);
        setCanceledOnTouchOutside(isCancelOutside);
    }



    public void setText(String  text){
        if (msgText!=null){
            msgText.setText(text);
        }



    }


    public static  LoadingDialog createDialog(Context context,int time,String text){
        if (loadingDialog==null){
            loadingDialog = new LoadingDialog(context,"",true,false,false);
            loadingDialog.show();
            loadingDialog.autoClose(time);
            loadingDialog.setInitText(text);
        }

        return  loadingDialog;

    }
    public static  LoadingDialog createDialog(Context context,int time){
        if (loadingDialog==null){
            loadingDialog = new LoadingDialog(context,"",true,false,false);
            loadingDialog.show();
            loadingDialog.autoClose(time);
            loadingDialog.setInitText("添加设备中");
        }

        return  loadingDialog;

    }
    public static  void closeDialog(){
        if (loadingDialog!=null){
            loadingDialog.dismiss();
            loadingDialog=null;

        }
    }

    CountDownTimer cdt;
    public  void autoClose(int second){
        if (cdt!=null){
            cdt.cancel();
            cdt=null;
        }
        cdt = new CountDownTimer(second*1000+50, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int number=(int)(millisUntilFinished/1000) ;
                String text="";
                if (number%3==0){
                    text=number+"...";
                } if (number%3==1){
                    text=number+".. ";
                } if (number%3==2){
                    text=number+".  ";
                }
                setText(initText+text);
            }

            @Override
            public void onFinish() {
                //倒计时3秒结束时对话框消失
               dismiss();

            }
        };
        cdt.start();
    }

    @Override
    public void dismiss() {
        if (cdt!=null){
            cdt.cancel();
            cdt=null;
        }
        super.dismiss();

    }
}