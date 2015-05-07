package com.jerry.plugin;

import java.util.Arrays;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * 
 * @author jerry
 * @goal "zkcu"
 */
@Mojo(name = "zkcu", requiresDirectInvocation = true)
public class ZkcuMojo extends AbstractMojo {
	@Parameter(required = true)
	private String zk;

	@Parameter(required = false)
	private String[] zkConfigurableClass;

	@Parameter(required = true)
	private String mainClass;

	@Parameter(required = true, defaultValue = "")
	private String rootPath;

	@Parameter(required = true, readonly = true, defaultValue = "${project.build.outputDirectory}")
	private String classesDirectory;

	public void execute() throws MojoExecutionException {
		this.getLog().info("Details:");
		this.getLog().info("		zk : " + this.zk);
		this.getLog().info("		classesDirectory : " + this.classesDirectory);
		this.getLog().info(
				"		zkConfigurableClass : "
						+ (this.zkConfigurableClass == null ? "" : Arrays
								.toString(this.zkConfigurableClass)));
		this.getLog().info("		mainClass : " + this.mainClass);
		this.getLog().info("		rootPath : " + this.rootPath);

		if (null == this.zkConfigurableClass
				|| this.zkConfigurableClass.length == 0) {
			return;
		}

		String line = new String("\r\n");
		StringBuilder body = new StringBuilder();

		if (this.rootPath.trim().isEmpty()) {
			body.append(
					"com.jerry.zkconfigutil.app.ZkConfigUtil zkConfigUtil = new com.jerry.zkconfigutil.app.ZkConfigUtil(\"")
					.append(this.zk).append("\");").append(line);
		} else {
			body.append(
					"com.jerry.zkconfigutil.app.ZkConfigUtil zkConfigUtil = new com.jerry.zkconfigutil.app.ZkConfigUtil(\"")
					.append(this.zk).append("\", \"").append(this.rootPath)
					.append("\");").append(line);
		}

		body.append("Class cla = null;").append(line);

		for (String cla : this.zkConfigurableClass) {
			body.append("cla = Class.forName(\"").append(cla).append("\");")
					.append(line);
			body.append("zkConfigUtil.register(cla, true);").append(line);
		}
		try {
			ClassPool pool = ClassPool.getDefault();
			pool.insertClassPath(new ClassClassPath(this.getClass()));
			pool.insertClassPath(this.classesDirectory);
			CtClass cc = pool.get(this.mainClass);
			CtMethod cm = cc.getDeclaredMethod("main");
			cm.insertBefore(body.toString());
			cc.writeFile(this.classesDirectory);
		} catch (Exception e) {
			this.getLog().error(e);
		}
	}
}