package de.tr0llhoehle.buschtrommel.test.mockups;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import de.tr0llhoehle.buschtrommel.models.Message;

public class FileContentMock {

	public static final String contentA = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc nibh elit, laoreet et fermentum nec, vestibulum at mauris. Aenean mollis dapibus purus vitae tincidunt. Etiam blandit magna id erat blandit lacinia. Proin ullamcorper sagittis nunc nec rutrum. Quisque tempus augue eget metus tincidunt iaculis. Praesent ultricies vestibulum purus ac luctus. Phasellus eget arcu eu ipsum gravida ullamcorper. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Pellentesque rutrum arcu pretium nunc pellentesque gravida. Aliquam erat volutpat. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Cras eleifend, nulla eget viverra ultricies, neque nisl hendrerit libero, ac aliquet lectus neque et urna. Cras quis sem eget dui consequat ullamcorper vel sit amet dui. Cras eget rutrum erat. Nunc non est a mi iaculis mattis. Cras quis leo urna. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Donec vulputate, libero et tempor pretium, turpis massa iaculis dolor, non viverra orci nisi ullamcorper ligula. Cras porttitor molestie dolor, quis iaculis nisi sagittis a. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Curabitur aliquam facilisis felis sed facilisis. Phasellus convallis consequat hendrerit. Donec porta dui tincidunt mi venenatis in consequat lacus ornare. Fusce volutpat, tellus sit amet vulputate venenatis, ipsum leo aliquet sapien, eget cursus odio ligula eget ipsum. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Vivamus sed neque vitae nunc sollicitudin tristique. Aliquam vel rutrum lectus. Aliquam erat volutpat. Cras ut placerat justo. Pellentesque non est eros, eget convallis mauris. Etiam semper mi at ante commodo et ullamcorper felis mattis. Sed ac purus dolor, ut consequat tellus. Aenean lectus sapien, volutpat quis pharetra vitae, vehicula eu orci. Curabitur sed odio lacus. Quisque vel mi sed mi malesuada orci aliquam.";

	/**
	 * Reads the content of al file as String
	 * 
	 * @param relPath
	 * @return
	 * @throws IOException
	 */
	public static String getFileContent(String relPath) throws IOException {
		java.io.BufferedReader reader = new BufferedReader(new FileReader(relPath));
		StringBuilder result = new StringBuilder();
		while (reader.ready())
			result.append(reader.readLine());
		reader.close();
		return result.toString();
	}

	/**
	 * Writes a given string into a file.
	 * 
	 * The writer won't append
	 * 
	 * @param relPath
	 * @param content
	 * @throws IOException
	 */
	public static void writeFileContent(String relPath, String content) throws IOException {
		java.io.Writer writer = new java.io.FileWriter(relPath, false);
		writer.write(content);
		writer.close();
	}

	/**
	 * Converts a string into an input stream. Uses the encoding, that was specified in the message class
	 * @param str
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static InputStream getStringAsStream(String str) throws UnsupportedEncodingException {
		return new java.io.ByteArrayInputStream(str.getBytes(Message.ENCODING));
	}

}
