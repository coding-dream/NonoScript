package com.nono.apkkiller;

import java.io.File;
import java.util.Arrays;

import com.nono.apkkiller.util.ExternalCommand;
import org.apache.commons.io.FileUtils;

public final class Main {
	static String originApk = "D:\\AndroidKillerProject\\5555.apk";

	public static void main(final String[] args) throws Exception {
		File originFile = new File(originApk);

		String path = "D:\\AndroidKillerProject\\";
		String destPath = path + "output\\" + originFile.getName().substring(0, originFile.getName().length() - 4);
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}

		// 这里在windows下一定要加/c参数 ,否则会报错,/c是 执行字符串指定的命令然后终止
		// 如gradle的用法：commandLine 'cmd', '/c', 'dx --dex --output=' + outputPath + " " + sourcePath
		String apkToolCmd = "cmd /c apktool d " + " " + originApk + " -o " + destPath;

		File copyZip = new File(originFile.getName().substring(0, originFile.getName().length() -4) + ".zip");
		FileUtils.copyFile(originFile, copyZip);

		String unzip = "cmd /c 7z x " + copyZip;

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
