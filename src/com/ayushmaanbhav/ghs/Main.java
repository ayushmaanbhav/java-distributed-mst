package com.ayushmaanbhav.ghs;

import java.io.File;
import java.util.Scanner;

public class Main {
	public static void main(String args[]) throws Exception {
		// Network.initialise(loadGraph(args[1]));
		Network.initialise(loadGraph("data.txt"));
		Network.startGHS();
		new PollingService().start();
		Events.startRecording();
	}

	public static double[][] loadGraph(String file) throws Exception {
		Scanner scanner = new Scanner(new File(file));
		int N = scanner.nextInt();
		double[][] adjcencyM = new double[N][N];
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				adjcencyM[i][j] = scanner.nextDouble();
			}
		}
		scanner.close();
		return adjcencyM;
	}
}
