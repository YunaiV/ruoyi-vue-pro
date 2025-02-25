package cn.iocoder.yudao.framework.common.util.spring;


import cn.hutool.core.util.ClassUtil;
import cn.iocoder.yudao.framework.common.util.io.FileUtils;

import java.io.File;

public class MavenProject extends Project {

	private File pomFile=null;
	private File targetDir=null;
	private File targetClassesDir=null;
	private File mainSourceDir=null;
	private File mainResourceDir=null;
	private File testSourceDir=null;
	private File testResourceDir=null;

	/**
	 * 在指定路径上寻找Maven项目
	 * */
	public MavenProject(File projectDir) {
		this.init(projectDir);
		initFileAndDir();
	}

	public  boolean hasPomFile() {
		return this.getPomFile()!=null && this.getPomFile().exists();
	}

	public  boolean hasMainSourceDir() {
		return this.getMainSourceDir()!=null && this.getMainSourceDir().exists();
	}

	public  boolean hasTargetDir() {
		return this.getTargetDir()!=null && this.getTargetDir().exists();
	}

	/**
	 * 在指定路径上寻找Maven项目
	 * */
	public MavenProject(String path) {
		this(new File(path));
	}

	public void init(File projectDir) {

		while(true) {
			this.projectDir = projectDir;
			identityFile=FileUtils.resolveByPath(projectDir,"pom.xml");
//			File src=FileUtil.resolveByPath(projectDir,"src");
			if(identityFile.exists()) {
				break;
			}
			projectDir=projectDir.getParentFile();
			if(projectDir==null) {
				throw new RuntimeException("no maven project in path");
			}
		}
	}





	public MavenProject() {
		Class clz= ClassUtil.getClass((new Throwable()).getStackTrace()[1].getClassName());
		init(clz);
	}

	public MavenProject(Class clz) {
		this.init(clz);
	}

	private void init(Class clz) {
		super.init(clz,"pom.xml");
		initFileAndDir();
	}


	private void initFileAndDir() {
		if(projectDir==null || !projectDir.exists()) return;
		//
		this.pomFile=this.getIdentityFile();
		this.targetDir=FileUtils.resolveByPath(this.getProjectDir(),"target");
		this.targetClassesDir=FileUtils.resolveByPath(this.getProjectDir(),"target","classes");
		this.mainSourceDir=FileUtils.resolveByPath(this.getProjectDir(),"src","main","java");
		this.mainResourceDir=FileUtils.resolveByPath(this.getProjectDir(),"src","main","resources");
		this.testSourceDir=FileUtils.resolveByPath(this.getProjectDir(),"src","test","java");
		this.testResourceDir=FileUtils.resolveByPath(this.getProjectDir(),"src","test","resources");
	}

	public File getSourceDir(String dirName) {
		return FileUtils.resolveByPath(this.getProjectDir(),"src",dirName,"java");
	}

	public File getResourcesDir(String dirName) {
		return FileUtils.resolveByPath(this.getProjectDir(),"src",dirName,"resources");
	}

	public String getSourcePath(String targetPath) {

		//针对 window 的特殊处理
		targetPath=targetPath.replace('\\', '/');
		String baseTargetPath=this.getTargetDir().getAbsolutePath().replace('\\', '/');
		if(!targetPath.startsWith(baseTargetPath)){
			throw new RuntimeException("not int target path of this project , "+this.projectDir.getAbsolutePath());
		}
		int i=FileUtils.resolveByPath(this.getTargetDir(),"classes").getAbsolutePath().length();
		return FileUtils.resolveByPath(this.getMainSourceDir(),targetPath.substring(i)).getAbsolutePath();

	}

	public File getSourceFile(Class clz) {
		return this.getSourceFile(this.mainSourceDir,this.testSourceDir,clz);
	}

	public File getSourceFile(File sourceDir, File testSourceDir ,Class clz) {
		File file=FileUtils.resolveByClass(clz);
		String rel=FileUtils.getRelativePath(targetClassesDir, file);
		rel=FileUtils.changeExtName(rel,".java");
		File src=FileUtils.resolveByPath(sourceDir, rel);
		if(src.exists()) {
			return src;
		}
		if(testSourceDir!=null) {
			src = FileUtils.resolveByPath(testSourceDir, rel);
			if (src.exists()) {
				return src;
			}
		}
		//
		return null;
	}

	public File getPomFile() {
		return pomFile;
	}

	public File getTargetDir() {
		return targetDir;
	}

	public File getTargetClassesDir() {
		return targetClassesDir;
	}

	public File getMainSourceDir() {
		return mainSourceDir;
	}

	public File getTestSourceDir() {
		return testSourceDir;
	}

	public File getMainResourceDir() {
		return mainResourceDir;
	}

	public File getTestResourceDir() {
		return testResourceDir;
	}

}
