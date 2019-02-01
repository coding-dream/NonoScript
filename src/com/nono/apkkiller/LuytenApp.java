package com.nono.apkkiller;

import com.nono.apkkiller.util.ExternalCommand;
import com.nono.apkkiller.util.ThreadUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class LuytenApp {

	// 可控参数，注意这里的文件目录不要带上 ==== 等特殊字符，否则7z识别不了。
	static String originApk = "D:\\tempDelete\\huhu.apk";
	static String rootProject = "D:\\tempDelete\\";

	static File originFile = new File(originApk);

	static String fileName = originFile.getName().substring(0, originFile.getName().length() - 4);
	static String outputPath = rootProject + "output\\" + fileName + "\\";

	static String dexRootFolder = outputPath + "dex\\";


	public static void main(final String[] args) throws Exception {
		File file = new File(rootProject);
		if (!file.exists()) {
			file.mkdirs();
		}
		decodeDexAndUnzip();
		d2j_dex2jar();
		openLuyten();
	}

	private static void openLuyten() {
		List<File> jars = loadJarPaths();
		for (File jar : jars) {
			ThreadUtil.run(() -> executeAndPrintLines("cmd /c java -jar D:\\反编译工具\\Luyten\\Luyten.app\\Contents\\Resources\\Java\\luyten-0.4.9.jar " + jar.getAbsolutePath()));
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
