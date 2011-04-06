/*
 * Copyright 2011 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.liftweb.sbt

import java.io.{Reader, Writer}
import java.nio.charset.Charset
import org.mozilla.javascript.ErrorReporter
import org.mozilla.javascript.EvaluatorException
import com.yahoo.platform.yui.compressor.CssCompressor
import com.yahoo.platform.yui.compressor.JavaScriptCompressor
import _root_.sbt._


/**
 * CSS and Javascript compressor using YUI Compressor.
 *
 * @author Indrajit Raychaudhuri
 */
trait YuiCompressor extends BasicScalaProject with MavenStyleScalaPaths {

  /**
   * The extension of CSS files, defaults to "css".
   */
  val yuiCompressCssExt = "css"
  lazy val yuiCompressCssPattern = "*." + yuiCompressCssExt

  /**
   * The extension of JS files, defaults to "js".
   */
  val yuiCompressJsExt  = "js"
  lazy val yuiCompressJsPattern = "*." + yuiCompressJsExt

  /**
   * The suffix of CSS and JS output files, defaults to "-min".
   */
  def yuiCompressMinSuffix = "-min"
  lazy val yuiCompressCssMinPattern = "*%s.%s".format(yuiCompressMinSuffix, yuiCompressCssExt)
  lazy val yuiCompressJsMinPattern  = "*%s.%s".format(yuiCompressMinSuffix, yuiCompressJsExt)

  /**
   * The charset used to read and write the files, defaults to "UTF-8".
   */
  def yuiCompressCharset = Charset.forName("UTF-8")

  /**
   * The line-break column, defaults to 0.
   */
  def yuiCompressBreakCol = 0

  /**
   * Should obfuscate in addition to minification, defaults to `false`.
   */
  def yuiCompressMunge = false

  /**
   * Should preserve all semicolons, defaults to `false`.
   */
  def yuiCompressPreserveSemi = false

  /**
   * Should disable all micro optimizations, defaults to `false`.
   */
  def yuiCompressDisableOptimize = false


  def yuiCompressResourcesPath       = mainResourcesPath
  def yuiCompressResourcesOutputPath = mainResourcesOutputPath

  def yuiCompressResources       = descendents(yuiCompressResourcesPath ##, yuiCompressCssPattern | yuiCompressJsPattern)
  def yuiCompressResourcesOutput = descendents(yuiCompressResourcesOutputPath ##, yuiCompressCssMinPattern | yuiCompressJsMinPattern)

  lazy val YuiCompressDescription = "Compresses CSS and JS files, if available, using YUI Compressor."
  lazy val yuiCompress = yuiCompressAction

  protected def yuiCompressAction = yuiCompressTask(yuiCompressResources, yuiCompressResourcesOutputPath) describedAs YuiCompressDescription

  protected def yuiCompressTask(sources: => PathFinder, targetDir: => Path): Task = {
    import Path._
    val filePairs = Map() ++ (
      sources.get map { s =>
        (s, fromFile(fromString(targetDir, s.relativePath).asFile.getParentFile) / "%s%s.%s".format(s.base, yuiCompressMinSuffix, s.ext))
      })

    fileTask("yui-compress", filePairs.values.toList) {
      filePairs foreach { m => compressTask(m._1, m._2, log) }
      None
    }
  }

  private def compressTask(in: Path, out: Path, log: Logger): Option[String] = {
    import FileUtilities._
    lazy val (inFile, outFile) = (in.asFile, out.asFile)
    createDirectory(outFile.getParentFile, log) orElse {
      read(inFile, yuiCompressCharset, log) { is =>
        write(outFile, yuiCompressCharset, log) { os =>
          if (in.ext == yuiCompressCssExt) compressCss(is, os, log)
          else compressJs(is, os, log)
        }
        None
      }
      None
    }
  }

  private def compressCss(in: Reader, out: Writer, log: Logger): Option[String] = {
    Control.trapUnit("Failed compressing CSS: ", log) {
      new CssCompressor(in).compress(
        out,
        yuiCompressBreakCol)
      None
    }
  }

  private def compressJs(in: Reader, out: Writer, log: Logger): Option[String] = {
    Control.trapUnit("Failed compressing JS: ", log) {
      new JavaScriptCompressor(in, jsErrorReporter).compress(
        out,
        yuiCompressBreakCol,
        yuiCompressMunge,
        log.getLevel == Level.Debug,
        yuiCompressPreserveSemi,
        yuiCompressDisableOptimize)
      None
    }
  }

  private val jsErrorReporter = new ErrorReporter {
    def error(message: String, sourceName: String, line: Int, lineSource: String, lineOffset: Int) {
      doLog(Level.Error, message, sourceName, line, lineSource, lineOffset)
    }

    def runtimeError(message: String, sourceName: String, line: Int, lineSource: String, lineOffset: Int): EvaluatorException = {
      doLog(Level.Error, message, sourceName, line, lineSource, lineOffset)
      new EvaluatorException(message, sourceName, line, lineSource, lineOffset)
    }

    def warning(message: String, sourceName: String, line:Int, lineSource: String, lineOffset: Int) {
      doLog(Level.Warn, message, sourceName, line, lineSource, lineOffset)
    }

    def doLog(l: Level.Value, message: String, sourceName: String, line: Int, lineSource: String, lineOffset: Int) {
      if (line < 0) log.log(l, message)
      else log.log(l, "%s at %d [%d:%d] %s".format(sourceName, lineSource, line, lineOffset, message))
    }
  }

  override def packagePaths = super.packagePaths +++ yuiCompressResourcesOutput

  lazy val desc =
    BasicScalaProject.CopyResourcesDescription +
    " Additionally, compresses CSS and JS files, if available, using YUI Compressor."
  override def copyResourcesAction =
    yuiCompressAction dependsOn(super.copyResourcesAction) describedAs desc

}


trait WebYuiCompressor extends BasicWebScalaProject with MavenStyleWebScalaPaths with YuiCompressor {

  // Modify YuiCompressor paths
  override def yuiCompressResourcesPath       = webappPath
  override def yuiCompressResourcesOutputPath = temporaryWarPath
  override def extraWebappFiles               = super.extraWebappFiles +++ yuiCompressResourcesOutputPath
  // TODO: this should be optional
  // override def webappResources = super.webappResources --- yuiCompressResourcesPath

}
