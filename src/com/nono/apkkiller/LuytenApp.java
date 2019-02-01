package com.nono.apkkiller;

import com.nono.apkkiller.util.ExternalCommand;
import com.nono.apkkiller.util.ThreadUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class LuytenApp {

	static String rootProject = "D:\\tempDelete\\";

	// 需要反编译的apk的目录(批量反编译打开dex)
    static String originApkDirStr = "D:\\1XP模块apks\\";
    static File originApkDir = new File(originApkDirStr);

	public static void main(final String[] args) throws Exception {
		File file = new File(rootProject);
		if (!file.exists()) {
			file.mkdirs();
		}
		int startIndex = 5;
		int endIndex = 10;
        File[] fileList = originApkDir.listFiles();
        for (int i = startIndex; i < fileList.length; i++) {
            File apkFile = fileList[startIndex];
            if (!apkFile.getName().endsWith(".apk")) {
                continue;
            }
            if (i >= endIndex) {
                return;
            }
            startDecode(apkFile);
        }
	}

    private static void startDecode(File apkFile) {
        System.out.println("===========> decode " + apkFile.getAbsolutePath());
        String fileName = apkFile.getName().substring(0, apkFile.getName().length() - 4);
        String outputPath = rootProject + "output\\" + fileName + "\\";
        String dexRootFolder = outputPath + "dex\\";

        decodeDexAndUnzip(apkFile, dexRootFolder);
        d2j_dex2jar(dexRootFolder);
        openLuyten(dexRootFolder);
    }

    private static void openLuyten(String dexRootFolder) {
        List<File> jars = loadJarPaths(dexRootFolder);
        for (File jar : jars) {
            ThreadUtil.run(() -> executeAndPrintLines("cmd /c java -jar D:\\反编译工具\\Luyten\\Luyten.app\\Contents\\Resources\\Java\\luyten-0.4.9.jar " + jar.getAbsolutePath()));
        }
    }

	private static void d2j_dex2jar(String dexRootFolder) {
		List<File> dexPaths = loadDexPaths(dexRootFolder);
		for (File dex : dexPaths) {
			System.out.println(dex.getParent());
			executeAndPrintLines("cmd /c d2j-dex2jar " + dex.getAbsolutePath() + " -o " + dex.getAbsolutePath() + ".jar");
		}
	}

	private static List<File> loadPaths(String sufix, String dexRootFolder) {
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

	private static List<File> loadDexPaths(String dexRootFolder) {
		return loadPaths(".dex", dexRootFolder);
	}

	private static List<File> loadJarPaths(String dexRootFolder) {
		return loadPaths(".jar", dexRootFolder);
	}

	private static void decodeDexAndUnzip(File originFile, String dexRootFolder) {
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
