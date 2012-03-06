/*
 * Copyright (c) 2012 Michael Vorburger
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
 * explanation of the license and how it is applied.
 */
package ch.vorburger.exec;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

import ch.vorburger.exec.Platform.Type;
import ch.vorburger.mariadb4j.MariaDB4jException;

/**
 * Tests ManagedProcess.
 * 
 * @author Michael Vorburger
 */
public class ManagedProcessTest {

	@Test
	public void testBasics() throws Exception {
		ManagedProcess p = new ManagedProcessBuilder("someExec").build();
		assertThat(p.isAlive(), is(false));
		try {
			p.destroy();
			Assert.fail("Should have thrown an IllegalStateException");
		} catch (IllegalStateException e) {
			// as expected
		}
		try {
			p.exitValue();
			Assert.fail("Should have thrown an IllegalStateException");
		} catch (IllegalStateException e) {
			// as expected
		}
		try {
			p.start();	
			Assert.fail("Should have thrown an IOException");
		} catch (IOException e) {
			// as expected
		}
//		try {
//			p.waitFor();
//			Assert.fail("Should have thrown an IllegalStateException");
//		} catch (IllegalStateException e) {
//			// as expected
//		}
//		try {
//			p.waitFor(1234);
//			Assert.fail("Should have thrown an IllegalStateException");
//		} catch (IllegalStateException e) {
//			// as expected
//		}
//		try {
//			p.waitFor("Never say never...");
//			Assert.fail("Should have thrown an IllegalStateException");
//		} catch (IllegalStateException e) {
//			// as expected
//		}
//		try {
//			p.waitForAndDestroy(1234);
//			Assert.fail("Should have thrown an IllegalStateException");
//		} catch (IllegalStateException e) {
//			// as expected
//		}
	}
	
	@Test
	public void testSelfTerminatingExec() throws Exception {
		ManagedProcessBuilder pb;
		switch (Platform.is()) {
		case Windows:
			pb = new ManagedProcessBuilder("cmd.exe").addArgument("/C").addArgument("dir").addArgument("/X");
			break;

		case Mac:
		case Linux:
		case Solaris:
			pb = new ManagedProcessBuilder("true").addArgument("--version");
			break;

		default:
			throw new MariaDB4jException("Unexpected Platform, improve the test dude...");
		}

		ManagedProcess p = pb.build();
		assertThat(p.isAlive(), is(false));
		p.setConsoleBufferMaxLines(5);
		p.start();
		assertThat(p.isAlive(), is(true));
		p.waitFor("bytes free"); // just an illustration
		p.waitForSuccess();
		p.exitValue(); // just making sure it works, don't check, as Win/NIX diff.
		assertThat(p.isAlive(), is(false));
		
		String recentConsoleOutput = p.getConsole();
		assertTrue(recentConsoleOutput.length() > 10);
		assertTrue(recentConsoleOutput.contains("\n"));
		System.out.println("Recent " + p.getConsoleBufferMaxLines() + " lines of console output:");
		System.out.println(recentConsoleOutput);
	}

	@Test
	public void testMustTerminateExec() throws Exception {
		ManagedProcessBuilder pb;
		if (Platform.is(Type.Windows)) {
			pb = new ManagedProcessBuilder("notepad.exe");
		} else {
			pb = new ManagedProcessBuilder("vi"); // TODO ?
		}
		
		ManagedProcess p = pb.build();
		assertThat(p.isAlive(), is(false));
		p.start();
		assertThat(p.isAlive(), is(true));
		p.waitForOrDestroy(200);
		assertThat(p.isAlive(), is(false));
		// can not: p.exitValue();
	}

}
