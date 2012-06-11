/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository.api;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.Closeables;

import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.CatCommand;
import sonia.scm.repository.spi.CatCommandRequest;

//~--- JDK imports ------------------------------------------------------------

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 * Shows the content of a file in the {@link Repository}.
 *
 * @author Sebastian Sdorra
 * @since 1.17
 */
public final class CatCommandBuilder
{

  /**
   * Constructs a new {@link CatCommandBuilder}, this constructor should
   * only be called from the {@link RepositoryService}.
   *
   * @param catCommand implementation of the {@link CatCommand}
   */
  CatCommandBuilder(CatCommand catCommand)
  {
    this.catCommand = catCommand;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Reset each parameter to its default value.
   *
   * @return {@code this}
   */
  public CatCommandBuilder reset()
  {
    request.reset();

    return this;
  }

  /**
   * Passes the content of the given file to the outputstream.
   *
   * @param outputStream outputstream for the content
   * @param path file path
   *
   * @return {@code this}
   */
  public CatCommandBuilder retriveContent(OutputStream outputStream,
          String path)
  {
    getCatResult(outputStream, path);

    return this;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the content of the given file.
   *
   * @param path file path
   * @return content of the file
   */
  public String getContent(String path)
  {
    String content = null;
    ByteArrayOutputStream baos = null;

    try
    {
      baos = new ByteArrayOutputStream();
      getCatResult(baos, path);
      content = baos.toString();
    }
    finally
    {
      Closeables.closeQuietly(baos);
    }

    return content;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Sets the revision of the file.
   *
   *
   * @param revision revision of the file
   *
   * @return {@code this}
   */
  public CatCommandBuilder setRevision(String revision)
  {
    request.setRevision(revision);

    return this;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Executes the cat command.
   *
   *
   * @param outputStream the outputstream for the content
   * @param path path of the file
   */
  private void getCatResult(OutputStream outputStream, String path)
  {
    Preconditions.checkNotNull(outputStream, "OutputStream is required");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(path),
                                "path is required");

    CatCommandRequest requestClone = request.clone();

    requestClone.setPath(path);
    catCommand.getCatResult(requestClone, outputStream);
  }

  //~--- fields ---------------------------------------------------------------

  /** implementation of the cat command */
  private CatCommand catCommand;

  /** request for the cat command */
  private CatCommandRequest request = new CatCommandRequest();
}
