/*
package cn.iocoder.yudao.module.report.framework.ureport.provider;

import com.bstek.ureport.exception.ReportException;
import com.bstek.ureport.provider.report.ReportFile;
import com.bstek.ureport.provider.report.ReportProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

*/
/**
 * 自定义文件存储，可接入oss等
 * @author 赤焰
 *//*

@Slf4j
@Component
@ConfigurationProperties(prefix = "ureport.provider.file")
public class UreportFileReportProvider implements ReportProvider{

    private String prefix="reportFile:";
    private String fileStoreDir;
    private boolean disabled;

    @Override
    public InputStream loadReport(String file) {
        if(file.startsWith(prefix)){
            file=file.substring(prefix.length(),file.length());
        }
        String fullPath=fileStoreDir+"/"+file;
        try {
            return new FileInputStream(fullPath);
        } catch (FileNotFoundException e) {
            throw new ReportException(e);
        }
    }

    @Override
    public void deleteReport(String file) {
        if(file.startsWith(prefix)){
            file=file.substring(prefix.length(),file.length());
        }
        String fullPath=fileStoreDir+"/"+file;
        File f=new File(fullPath);
        if(f.exists()){
            f.delete();
        }
    }

    @Override
    public List<ReportFile> getReportFiles() {
        File file=new File(fileStoreDir);
        List<ReportFile> list= new ArrayList<>();
        for(File f: Objects.requireNonNull(file.listFiles())){
            Calendar calendar=Calendar.getInstance();
            calendar.setTimeInMillis(f.lastModified());
            list.add(new ReportFile(f.getName(),calendar.getTime()));
        }
        list.sort((f1, f2) -> f2.getUpdateDate().compareTo(f1.getUpdateDate()));
        return list;
    }

    @Override
    public String getName() {
        return "文件存储系统";
    }

    @Override
    public void saveReport(String file,String content) {
        if(file.startsWith(prefix)){
            file=file.substring(prefix.length(),file.length());
        }
        String fullPath=fileStoreDir+"/"+file;
        FileOutputStream outStream=null;
        try{
            outStream=new FileOutputStream(new File(fullPath));
            IOUtils.write(content, outStream,"utf-8");
        }catch(Exception ex){
            throw new ReportException(ex);
        }finally{
            if(outStream!=null){
                try {
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public boolean disabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public void setFileStoreDir(String fileStoreDir) {
        this.fileStoreDir = fileStoreDir;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

}
*/
