/*
 * Copyright (C) 2017. Miroslav Kopecky
 * This CompilerProvider.java  is part of robo4j.
 * path: /Users/miroslavkopecky/GiTHub_MiroKopecky/robo4j-tools/compiler/robo4j-center/src/main/java/com/robo4j/tools/center/provider/CompilerProvider.java
 * module: robo4j-center_main
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.tools.center.provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import com.robo4j.tools.center.CenterException;
import com.robo4j.tools.center.enums.SupportedOS;
import com.robo4j.tools.center.property.TaskProperties;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class CompilerProvider {

	private static final String ENDING_JAVA = ".java";
	private static final String PROJECT_SRC_MAIN_JAVA = "src.main".concat(ENDING_JAVA);
	private static final String PROJECT_SRC_MAIN_RESOURCES = "src.main.resources";
	private static final String DOT_DELIMITER = ".";
	private static final String JAVA_MANIFEST_MF = "MANIFEST.MF";
	private static final String JAVA_META_INF = "META-INF";
	private static final String PROJECT_LIBS = "libs";
	private static final String EMPTY_STIRNG = "";

	private final TaskProperties properties;
	private boolean compiled;
    private List<Path> excludedPaths;

    public CompilerProvider(TaskProperties properties) {
		this.properties = properties;
	}

	public boolean compile() throws Exception {

		final SupportedOS os = properties.getDetectedSystem();
		Path mainSrcPath = Paths.get(correctedPath(os, PROJECT_SRC_MAIN_JAVA));
		Path mainResourcesPath = Paths.get(correctedPath(os, PROJECT_SRC_MAIN_RESOURCES));

		File outDir = new File(correctedPath(os, String.join(DOT_DELIMITER, properties.getOutputDirectory())));
		boolean createdDir = outDir.mkdir();

		List<String> compilerOptions = Arrays.asList("-d", outDir.getAbsolutePath(), "-cp",
				properties.getOutputDirectory());

        excludedPaths = Arrays.asList(Paths.get(properties.getOutputDirectory().concat(properties.getSeparator()).concat("production")));
		List<Path> mainSrcPaths = searchFiles(new ArrayList<>(), mainSrcPath, excludedPaths);
		List<Path> inputResources = searchFiles(new ArrayList<>(), mainResourcesPath, excludedPaths);

		copyResources(os, inputResources);

		if (unzipLibraries(properties)) {
			List<File> files = mainSrcPaths.stream().map(Path::toFile).filter(f -> f.getName().endsWith(ENDING_JAVA))
					.collect(Collectors.toList());
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

			Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(files);
			boolean compileStatus = compiler.getTask(null, fileManager, null, compilerOptions, null, compilationUnits)
					.call();
			fileManager.close();

			if (compileStatus) {
				compiled = true;
				return compiled;
			} else {
				return compiled;
			}
		}

		return compiled;
	}

	public boolean createJar() throws Exception {
		if (compiled) {
		    Path jarFilePath = Paths.get(properties.getCompiledFilename().concat(".jar"));


		    if(jarFilePath.toFile().exists()){
		        Files.delete(jarFilePath);
            }
            Files.createFile(jarFilePath);
		    FileOutputStream fos = new FileOutputStream(jarFilePath.toFile());

			Path tmpPath = Paths.get(properties.getCompiledFilename());
			if (tmpPath.toFile().exists()) {
				deletePath(tmpPath);
			}
			Files.createDirectory(Paths.get(properties.getCompiledFilename()));

			Path metaDirPath = Paths.get(correctedPath(properties.getDetectedSystem(),
					String.join(DOT_DELIMITER, properties.getCompiledFilename(), JAVA_META_INF)));
			boolean metaDirState = metaDirPath.toFile().mkdir();
			Path manifestFilePath = Paths.get(correctedPath(properties.getDetectedSystem(), String.join(DOT_DELIMITER, properties.getCompiledFilename(), JAVA_META_INF, DOT_DELIMITER)).concat(JAVA_MANIFEST_MF));
			Files.createFile(manifestFilePath);
			InputStream fis = new FileInputStream(manifestFilePath.toFile());
			Manifest manifest = new Manifest(fis);
			Attributes attrs = manifest.getMainAttributes();
			attrs.putValue("Manifest-Version", "1.0");
			attrs.putValue("Main-Class", properties.getMainPackage().concat(DOT_DELIMITER)
					.concat(properties.getMainClass().replace(ENDING_JAVA, EMPTY_STIRNG)));

			JarOutputStream jarOut = new JarOutputStream(fos, manifest);

            List<Path> jarExcluded = new ArrayList<>(excludedPaths);
			jarExcluded.add(Paths.get(properties.getOutputDirectory().concat(properties.getSeparator()).concat("lejos")));
			jarExcluded.add(Paths.get(properties.getOutputDirectory().concat(properties.getSeparator())
					.concat(JAVA_META_INF).concat(properties.getSeparator()).concat("maven")));

            Path outPath = Paths.get(properties.getOutputDirectory());

            List<Path> pathToCopy = searchFiles(new ArrayList<>(), outPath, jarExcluded);
            for(Path p: pathToCopy){
				if(p.toFile().isDirectory()){
            		Path tmpDirPath = Paths.get(p.normalize().toString().replaceFirst(properties.getOutputDirectory(), properties.getCompiledFilename()));
					tmpDirPath.toFile().mkdir();
				} else if(p.toFile().isFile() && !p.getFileName().toString().startsWith(DOT_DELIMITER)) {
					Path tmpFilePath = Paths.get(p.normalize().toString().replaceFirst(properties.getOutputDirectory(), properties.getCompiledFilename()));
					try {
						Files.deleteIfExists(tmpFilePath);
						Files.copy(p, tmpFilePath);
					} catch (Exception e){
						throw new CenterException("jar creation", e);
					}
				}
			}


			List<Path> outToJar = searchFiles(new ArrayList<>(), tmpPath, jarExcluded);
			String fixString = properties.getCompiledFilename().concat(properties.getSeparator());
            for(Path path: outToJar){
				if(path.toFile().isDirectory()){
                     jarOut.putNextEntry(new ZipEntry(path.normalize().toString().concat(properties.getSeparator()).replace(fixString, EMPTY_STIRNG)));
                     jarOut.closeEntry();
                 } else {
                 	 if(!path.getFileName().toString().contains(".MF")){
						 jarOut.putNextEntry(new ZipEntry(path.normalize().toString().replace(fixString, EMPTY_STIRNG)));
						 jarOut.write(Files.readAllBytes(path));
						 jarOut.closeEntry();
					 }
                 }
            }
            jarOut.close();
            fos.close();
			return deletePath(tmpPath);

		} else {
            throw new ConnectException("not compiled");
        }

	}

	private boolean deletePath(Path path) throws Exception{
		Files.walk(path, FileVisitOption.FOLLOW_LINKS)
				.sorted(Comparator.reverseOrder())
				.map(Path::toFile)
				.forEach(File::delete);
		return true;
	}

	private String correctedPath(SupportedOS os, String mainPackage) {
		return mainPackage.replace(DOT_DELIMITER, os.getSeparator());
	}

	private boolean isExcludedChild(Path child, List<Path> path){
        return path.stream()
                .filter(child::startsWith)
                .count() > 0;
    }

	private List<Path> searchFiles(List<Path> result, Path directory, List<Path> exclude) throws Exception {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
			for (Path entry : stream) {
			    if(!isExcludedChild(entry, exclude)) {
                    if (entry.toFile().isDirectory()) {
                        result.add(entry);
                        searchFiles(result, entry, exclude);
                    } else {
                        result.add(entry);
                    }
                }
			}
		} catch (DirectoryIteratorException ex) {
			// I/O error during the iteration, the cause is an IOException
			throw new IOException(ex);
		}
		return result;
	}

	private void copyResources(SupportedOS os, List<Path> resourcePaths) throws Exception {

		String resourcesString = correctedPath(os, PROJECT_SRC_MAIN_RESOURCES);
		for (Path path : resourcePaths) {
		    if(!path.getFileName().toString().startsWith(DOT_DELIMITER)){
                StringBuilder sb = new StringBuilder(properties.getOutputDirectory()).append(os.getSeparator())
                        .append(path.normalize().toString().replace(resourcesString, EMPTY_STIRNG));
                Path targetPath = Paths.get(sb.toString());
                if (targetPath.toFile().exists() && targetPath.toFile().isFile()) {
                    Files.delete(targetPath);
                    Files.copy(path, targetPath);
                } else if(targetPath.toFile().isFile()){
                    Files.copy(path, targetPath);
                }
            }
		}
	}

	private boolean unzipLibraries(TaskProperties properties) throws Exception {
		Path libPath = Paths.get(PROJECT_LIBS.concat(properties.getSeparator()).concat(properties.getRobo4jLibrary()));
		return unzipJarFile(properties.getDetectedSystem(), properties.getOutputDirectory(), libPath);
	}

	private boolean unzipJarFile(SupportedOS os, String out, Path libPath) throws IOException {

		JarFile jar = new JarFile(libPath.toFile());
		Enumeration<JarEntry> jarEntries = jar.entries();

		while (jarEntries.hasMoreElements()) {
			JarEntry file = jarEntries.nextElement();
			java.io.File f = new File(out.concat(os.getSeparator()).concat(file.getName()));
			if (file.isDirectory()) {
				boolean status = f.mkdir();
				continue;
			}

			try (InputStream is = jar.getInputStream(file); FileOutputStream fos = new FileOutputStream(f)) {
				while (is.available() > 0) {
					fos.write(is.read());
				}
			}

		}

		return true;
	}
}
