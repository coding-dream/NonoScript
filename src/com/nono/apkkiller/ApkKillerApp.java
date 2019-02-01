package com.nono.apkkiller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nono.apkkiller.util.ExternalCommand;
import org.apache.commons.io.FileUtils;

/**
 * 反编译强力工具：ApkKiller
 *
 * 如果遇到 Caused by: java.io.IOException: can't find classes.dex in the zip 不需理会，是因为文件已经decode过一次的原因。
 */
public final class ApkKillerApp {

	// 可控参数
	static String originApk = "D:\\AndroidKillerProject\\huhu20190125.apk";
	static String rootProject = "D:\\AndroidKillerProject\\";
	static String killerRootProject = "D:\\Program Files\\AndroidKiller_v1.3.1\\projects\\";

	// 下面的不需要修改
	static File originFile = new File(originApk);

	static String fileName = originFile.getName().substring(0, originFile.getName().length() - 4);
	static String outputPath = rootProject + "output\\" + fileName + "\\";

	static String smaliRootFolder = outputPath + "smali";
	static String dexRootFolder = outputPath + "dex";
	static String classesRootFolder = outputPath + "classes";

	static File templateProject = new File(killerRootProject + fileName);

	public static void main(final String[] args) throws Exception {
		File file = new File(rootProject);
		if (!file.exists()) {
			file.mkdirs();
		}
		decodeDexAndUnzip();
		decodeSmali();
		d2j_dex2jar();
		mergeSmali();
		unzipJar();
		createKillerProject();
	}

	private static void unzipJar() {
		List<File> jars = loadJarPaths();
		for (File jar : jars) {
			String unzip = "cmd /c 7z x " + jar.getAbsolutePath() + " -o" + classesRootFolder;
			executeAndPrintLines(unzip);
		}
		System.out.println("unzip source success");
	}

	private static void mergeSmali() throws IOException {
		File rootFolder = new File(smaliRootFolder);
		File tempDst = new File(smaliRootFolder, "smali_rename");
		File finalDst = new File(smaliRootFolder, "smali");

		List<File> smaliChilds = new ArrayList<>();
		for (File smaliChildFolder : rootFolder .listFiles()) {
			if (smaliChildFolder.getName().startsWith("smali")) {
				System.out.println("current ==> copy directory: " + smaliChildFolder.getName());
				smaliChilds.add(smaliChildFolder);
				FileUtils.copyDirectory(smaliChildFolder, tempDst);
			}
		}
		// 删除旧的smali文件夹
		for (File smaliFo : smaliChilds) {
			System.out.println("delete start: " + smaliFo.getAbsolutePath());
			FileUtils.deleteDirectory(smaliFo);
			System.out.println("delete success: " + smaliFo.getAbsolutePath());
		}
		tempDst.renameTo(finalDst);
		System.out.println("merge success!");
	}

	private static void createKillerProject() {
		System.out.println("=========> begin create project: " + fileName + " <=========");
		String dir = System.getProperty("user.dir");
		File templateDir = new File(dir + "//src/template");
		try {
			// 该方法是把目录中的内容拷贝过去而不会拷贝父目录。
			FileUtils.copyDirectory(templateDir, templateProject);

			File killerProject = new File(templateProject, "Project");
			File killerProjectSrc = new File(templateProject, "ProjectSrc\\smali");

			// 拷贝处理完的文件到模板目录
			FileUtils.copyDirectory(new File(smaliRootFolder), killerProject);
			FileUtils.copyDirectory(new File(classesRootFolder), killerProjectSrc);
			List<File> jars = loadJarPaths();
			for (File jar : jars) {
				FileUtils.moveFileToDirectory(jar, killerProject, false);
			}
			System.out.println("Congratulation，The Project Success！");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void d2j_dex2jar() {
		List<File> dexPaths = loadDexPaths();
		for (File dex : dexPaths) {
			System.out.println(dex.getParent());
			executeAndPrintLines("cmd /c d2j-dex2jar " + dex.getAbsolutePath() + " -o " + dex.getAbsolutePath() + ".jar");
		}
	}

	private static List<File> loadPaths(String sufix) {
		List<File> dexs = new ArrayList<>();

		File fileRootDexPath = new File(dexRootFolder);
		if (!fileRootDexPath.isDirectory()) {
			throw new RuntimeException("this not dex directory!");
		}
		for (File dexFile : fileRootDexPath.listFiles()) {
			if (dexFile.getName().contains(sufix)) {
				dexs.add(dexFile);
			}
		}
		return dexs;
	}

	private static List<File> loadDexPaths() {
		return loadPaths(".dex");
	}

	private static List<File> loadJarPaths() {
		return loadPaths(".jar");
	}

	private static void decodeSmali() {
		// 这里在windows下一定要加/c参数 ,否则会报错,/c是 执行字符串指定的命令然后终止
		// 如gradle的用法：commandLine 'cmd', '/c', 'dx --dex --output=' + outputPath + " " + sourcePath
		String apkToolCmd = "cmd /c apktool d " + " " + originApk + " -o " + smaliRootFolder;
		executeAndPrintLines(apkToolCmd);
	}

	private static void decodeDexAndUnzip() {
		// 注意 -o和输出目录中不能有空格
		String unzip = "cmd /c 7z x " + originFile.getAbsolutePath() + " -o" + dexRootFolder;
		executeAndPrintLines(unzip);
	}

	private static void executeAndPrintLines(final String... commandAndArgs) {
		System.out.println("Execute: " + Arrays.asList(commandAndArgs));
		// コマンドをパースしてオブジェクト化
		final ExternalCommand cmd = ExternalCommand.parse(commandAndArgs);
		// タイムアウト時間に3000ミリ秒を指定しつつ実行
		final ExternalCommand.Result res = cmd.execute(3000);
		// 終了コードを確認する
		System.out.println("ExitCode: " + res.getExitCode());
		// コマンドの標準出力の内容から入力ストリームを生成してそこから再度内容を読み取る
		System.out.println("Stdout: ");
		for (final String line : res.getStdoutLines()) {
			System.out.println("1>  " + line);
		}
		// コマンドの標準エラーの内容から入力ストリームを生成してそこから再度内容を読み取る
		System.out.println("Stderr: ");
		for (final String line : res.getStderrLines()) {
			System.out.println("2>  " + line);
		}
		System.out.println();
	}
}
