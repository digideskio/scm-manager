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



package sonia.scm.filter;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import sonia.scm.SCMContext;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.user.User;
import sonia.scm.web.filter.HttpFilter;
import sonia.scm.web.filter.SecurityHttpServletRequestWrapper;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class SecurityFilter extends HttpFilter
{

  /** Field description */
  public static final String URL_AUTHENTICATION = "/api/rest/authentication";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param configuration
   */
  @Inject
  public SecurityFilter(ScmConfiguration configuration)
  {
    this.configuration = configuration;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   * @param chain
   *
   * @throws IOException
   * @throws ServletException
   */
  @Override
  protected void doFilter(HttpServletRequest request,
    HttpServletResponse response, FilterChain chain)
    throws IOException, ServletException
  {
    Subject subject = SecurityUtils.getSubject();

    String uri =
      request.getRequestURI().substring(request.getContextPath().length());

    if (!uri.startsWith(URL_AUTHENTICATION))
    {
      if (hasPermission(subject))
      {
        chain.doFilter(new SecurityHttpServletRequestWrapper(request,
          getUser(subject)), response);
      }
      else if (subject.isAuthenticated() || subject.isRemembered())
      {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
      }
      else if (configuration.isAnonymousAccessEnabled())
      {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
      }
      else
      {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      }
    }
    else
    {
      chain.doFilter(request, response);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param subject
   *
   * @return
   */
  protected boolean hasPermission(Subject subject)
  {
    return ((configuration != null) && configuration.isAnonymousAccessEnabled()) || subject.isAuthenticated() || subject.isRemembered();
  }

  /**
   * Method description
   *
   *
   * @param subject
   *
   * @return
   */
  private User getUser(Subject subject)
  {
    User user = null;

    if (subject.isAuthenticated() || subject.isRemembered())
    {
      user = subject.getPrincipals().oneByType(User.class);
    }
    else
    {
      user = SCMContext.ANONYMOUS;
    }

    return user;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ScmConfiguration configuration;
}
