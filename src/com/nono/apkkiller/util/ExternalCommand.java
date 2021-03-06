package com.nono.apkkiller.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;

/**
 * 外部コマンドを表わすオブジェクト.
 * 同期もしくは非同期で当該コマンドを実行するためのメソッドを提供する。
 */
public final class ExternalCommand {
	/**
	 * Apache Commons Execのコマンドライン・オブジェクト.
	 */
	private final CommandLine commandLine;
	/**
	 * コマンド実行時のカレント・ディレクトリ.
	 */
	private File workingDirectory = new File(".");
	/**
	 * コンストラクタ.
	 * 静的メソッドを介した初期化のみ許可する。
	 * @param args コマンドとその引数を表わす文字列配列
	 */
	private ExternalCommand(final String... args) {
		if (args.length == 0) {
			throw new IllegalArgumentException();
		}
		this.commandLine = CommandLine.parse(args[0]);
		for (final String arg : Arrays.copyOfRange(args, 1, args.length)) {
			this.commandLine.addArgument(arg);
		}
	}
	/**
	 * Apache Commons Execのコマンドライン・オブジェクトを返す.
	 * @return コマンドライン・オブジェクト
	 */
	public CommandLine getCommandLine() {
		return commandLine;
	}
	/**
	 * コマンド実行時のカレント・ディレクトリを返す.
	 * @return コマンド実行時のカレント・ディレクトリ
	 */
	public File getWorkingDirectory() {
		return workingDirectory;
	}
	/**
	 * コマンド実行時のカレント・ディレクトリを設定する.
	 * デフォルトでは{@code "."}が設定されている。
	 * @param dir コマンド実行時のカレント・ディレクトリ
	 */
	public void setWorkingDirectory(final File dir) {
		if (dir == null || !dir.isDirectory()) {
			throw new IllegalArgumentException();
		}
		this.workingDirectory = dir;
	}
	/**
	 * コマンド実行時のカレント・ディレクトリを設定する.
	 * デフォルトでは{@code "."}が設定されている。
	 * @param dirPath コマンド実行時のカレント・ディレクトリのパス
	 */
	public void setWorkingDirectory(final String dirPath) {
		setWorkingDirectory(new File(dirPath));
	}
	/**
	 * タイムアウト指定なしで同期実行する.
	 * @return 実行結果
	 */
	public Result execute() {
		return execute(0);
	}
	/**
	 * タイムアウト指定ありで同期実行する.
	 * @param timeoutMillis 実行を打ち切るまでのミリ秒
	 * @return 実行結果
	 */
	public Result execute(final long timeoutMillis) {
		// 標準出力を受け取るためのストリームを初期化
		final PipeOutputStream out = new PipeOutputStream();
		// 標準エラーを受け取るためのストリームを初期化
		final PipeOutputStream err = new PipeOutputStream();
		// ストリームを引数にしてストリームハンドラを初期化
		final PumpStreamHandler streamHandler = new PumpStreamHandler(out, err);
		// エグゼキュータを初期化
		final Executor exec = new DefaultExecutor();
		// タイムアウト指定の引数を確認
		if (timeoutMillis > 0) {
			// 1以上の場合のみ実際にエグゼキュータに対して設定を行う
			exec.setWatchdog(new ExecuteWatchdog(timeoutMillis));
		}
		// 終了コードによるエラー判定をスキップするよう指定
		exec.setExitValues(null);
		// コマンド実行時のカレント・ディレクトリを設定
		exec.setWorkingDirectory(workingDirectory);
		// ストリームハンドラを設定
		exec.setStreamHandler(streamHandler);
		
		try {
			// 実行して終了コードを受け取る（同期実行する）
			final int exitCode = exec.execute(commandLine);
			out.close();
			err.close();
			// 実行結果を呼び出し元に返す
			return new Result(exitCode, out, err);
		} catch (final ExecuteException e) {
			// 終了コード判定はスキップされるためこの例外がスローされるのは予期せぬ事態のみ
			// よって非チェック例外でラップして再スローする
			throw new RuntimeException(e);
		} catch (final IOException e) {
			// IOエラーの発生は予期せぬ事態
			// よって非チェック例外でラップして再スローする
			throw new RuntimeException(e);
		}
	}
	/**
	 * タイムアウト指定なしで非同期実行する.
	 * @return 実行結果にアクセスするためのFutureオブジェクト
	 */
	public Future<Result> executeAsynchronously() {
		return executeAsynchronously(0);
	}
	/**
	 * タイムアウト指定ありで非同期実行する.
	 * @param timeoutMillis 実行を打ち切るまでのミリ秒
	 * @return 実行結果にアクセスするためのFutureオブジェクト
	 */
	public Future<Result> executeAsynchronously(final long timeoutMillis) {
		final ExecutorService service = Executors.newSingleThreadExecutor();
		return service.submit(new Callable<Result>() {
			@Override
			public Result call() throws Exception {
				return ExternalCommand.this.execute(timeoutMillis);
			}
		});
	}
	/**
	 * 外部コマンド文字列を受け取りオブジェクトを初期化する.
	 * @param commandLine 外部コマンド文字列
	 * @return オブジェクト
	 */
	public static ExternalCommand parse(final String commandLine) {
		return new ExternalCommand(commandLine);
	}
	/**
	 * 外部コマンドとその引数を表わす文字列配列を受け取りオブジェクトを初期化する.
	 * @param commandLine 外部コマンドとその引数の配列
	 * @return オブジェクト
	 */
	public static ExternalCommand parse(final String... commandAndArgs) {
		return new ExternalCommand(commandAndArgs);
	}
	/**
	 * 実行結果を表わすオブジェクト.
	 */
	public static final class Result {
		/**
		 * 終了コード.
		 */
		private final int exitCode;
		/**
		 * 標準出力の内容にアクセスするための{@link PipeOutputStream}.
		 */
		private final PipeOutputStream stdout;
		/**
		 * 標準エラーの内容にアクセスするための{@link PipeOutputStream}.
		 */
		private final PipeOutputStream stderr;
		/**
		 * コンストラクタ.
		 * @param exitCode 終了コード
		 * @param stdout 標準出力の内容にアクセスするための{@link PipeOutputStream}
		 * @param stderr 標準エラーの内容にアクセスするための{@link PipeOutputStream}
		 */
		private Result(final int exitCode, final PipeOutputStream stdout, final PipeOutputStream stderr) {
			this.exitCode = exitCode;
			this.stdout = stdout;
			this.stderr = stderr;
		}
		/**
		 * 終了コードを返す.
		 * @return 終了コード
		 */
		public int getExitCode() {
			return exitCode;
		}
		/**
		 * 標準出力の内容にアクセスするための{@link InputStream}を返す.
		 * @return {@link InputStream}
		 */
		public InputStream getStdout() {
			return stdout.getInputStream();
		}
		/**
		 * 標準エラーの内容にアクセスするための{@link InputStream}を返す.
		 * @return {@link InputStream}
		 */
		public InputStream getStderr() {
			return stderr.getInputStream();
		}
		/**
		 * 標準出力の内容に行ごとにアクセスするための{@link Iterable}を返す.
		 * キャラクターセットにはJVMのデフォルト・キャラクターセットを使用する。
		 * @return {@link Iterable}
		 */
		public Iterable<String> getStdoutLines() {
			return getStdoutLines(Charset.defaultCharset());
		}
		/**
		 * 標準出力の内容に行ごとにアクセスするための{@link Iterable}を返す.
		 * @param charset キャラクターセット
		 * @return {@link Iterable}
		 */
		public Iterable<String> getStdoutLines(final Charset charset) {
			return readLines(stdout.getInputStream(), charset);
		}
		/**
		 * 標準エラーの内容に行ごとにアクセスするための{@link Iterable}を返す.
		 * キャラクターセットにはJVMのデフォルト・キャラクターセットを使用する。
		 * @return {@link Iterable}
		 */
		public Iterable<String> getStderrLines() {
			return getStderrLines(Charset.defaultCharset());
		}
		/**
		 * 標準エラーの内容に行ごとにアクセスするための{@link Iterable}を返す.
		 * @param charset キャラクターセット
		 * @return {@link Iterable}
		 */
		public Iterable<String> getStderrLines(final Charset charset) {
			return readLines(stderr.getInputStream(), charset);
		}
		/**
		 * ストリームから文字列を読み出し行ごとのリストに変換する.
		 * @param in ストリーム
		 * @return リスト
		 */
		private List<String> readLines(final InputStream in, final Charset cs) {
			final BufferedReader br = new BufferedReader(new InputStreamReader(in, cs));
			final List<String> result = new ArrayList<String>();
			String line = null;
			try {
				while ((line = br.readLine()) != null) {
					result.add(line);
				}
			} catch (final IOException e) {
				// 読み取り対象のストリームはByteArrayInputStreamである前提のため
				// IOエラーが起きることは実際上あり得ないこと
				// 万一エラーが起きた場合でも非チェック例外で包んで再スローする
				throw new RuntimeException(e);
			}
			return result;
		}
	}
}
