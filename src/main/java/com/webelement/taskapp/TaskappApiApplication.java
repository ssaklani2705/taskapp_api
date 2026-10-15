package com.webelement.taskapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class TaskappApiApplication {

	static boolean isAlpabetic(String s) {
		// "XYZ@GMAIL.COM" //
		// password validate
		return s.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
	}

	static String extractString(String param) {
		int index = param.indexOf("@");
		param = param.substring(index + 1);
		return param;
	}

	static String countOccuranceString(final byte[] byteArray) {
		Map<Byte, Integer> cacheMemory = new HashMap<>();
		for (int i = 0; i < byteArray.length; i++) {
			cacheMemory.merge(byteArray[i], 1, Integer::sum);
		}
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<Byte, Integer> entry : cacheMemory.entrySet()) {
			sb.append((char) (entry.getKey() & 0xFF)).append("=").append(entry.getValue()).append(" ");
		}

		return sb.toString().trim();
	}

	public static void main(String[] args) {

		SpringApplication.run(TaskappApiApplication.class, args);

	}

	static List<Integer> twoSum(int[] arr, int target) {
		Integer left = 0;
		Integer right = arr.length - 1;
		Integer sum = 0;
		List<Integer> tmp = new ArrayList<>();
		while (left < right) {
			sum = arr[left] + arr[right];
			if (sum == target) {
				tmp.add(left);
				tmp.add(right);
				return tmp;
			}
			if (sum < target) {
				left++;
			} else {
				right--;
			}
		}
		return tmp;
	}

	static List<Integer> moveZero(int[] arr) {

		Integer left = 0;
		Integer right = arr.length;
		Integer sum = 0;
		List<Integer> tmp = new ArrayList<>();
		while (left < right) {
			if (arr[left] != 0) {
				tmp.add(left);
				sum++;
			}
			left++;
		}
		for (int i = 0; i < sum; i++) {
			tmp.add(0);
		}
		return tmp;
	}

}
