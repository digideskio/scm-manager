/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.security;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.UsernamePasswordToken;

import org.junit.Test;

import sonia.scm.config.ScmConfiguration;
import sonia.scm.web.security.AuthenticationResult;
import sonia.scm.web.security.AuthenticationState;

//~--- JDK imports ------------------------------------------------------------

import java.util.concurrent.TimeUnit;

/**
 *
 * @author Sebastian Sdorra
 */
public class ConfigurableLoginAttemptHandlerTest
{

  /**
   * Method description
   *
   */
  @Test(expected = ExcessiveAttemptsException.class)
  public void testLoginAttemptLimitReached()
  {
    LoginAttemptHandler handler = createHandler(2, 2);
    UsernamePasswordToken token = new UsernamePasswordToken("hansolo", "hobbo");

    handler.beforeAuthentication(token);
    handler.onUnsuccessfulAuthentication(token, AuthenticationResult.FAILED);
    handler.beforeAuthentication(token);
    handler.onUnsuccessfulAuthentication(token, AuthenticationResult.FAILED);
    handler.beforeAuthentication(token);
  }

  /**
   * Method description
   *
   *
   * @throws InterruptedException
   */
  @Test
  public void testLoginAttemptLimitTimeout() throws InterruptedException
  {
    LoginAttemptHandler handler = createHandler(2, 1);
    UsernamePasswordToken token = new UsernamePasswordToken("hansolo", "hobbo");

    handler.beforeAuthentication(token);
    handler.onUnsuccessfulAuthentication(token, AuthenticationResult.FAILED);
    handler.beforeAuthentication(token);
    handler.onUnsuccessfulAuthentication(token, AuthenticationResult.FAILED);
    Thread.currentThread().sleep(TimeUnit.MILLISECONDS.toMillis(1200l));
    handler.beforeAuthentication(token);
  }

  /**
   * Method description
   *
   *
   * @throws InterruptedException
   */
  @Test
  public void testLoginAttemptResetOnSuccess() throws InterruptedException
  {
    LoginAttemptHandler handler = createHandler(2, 1);
    UsernamePasswordToken token = new UsernamePasswordToken("hansolo", "hobbo");

    handler.beforeAuthentication(token);
    handler.onUnsuccessfulAuthentication(token, AuthenticationResult.FAILED);
    handler.beforeAuthentication(token);
    handler.onUnsuccessfulAuthentication(token, AuthenticationResult.FAILED);

    handler.onSuccessfulAuthentication(token,
      new AuthenticationResult(AuthenticationState.SUCCESS));

    handler.beforeAuthentication(token);
    handler.onUnsuccessfulAuthentication(token, AuthenticationResult.FAILED);
    handler.beforeAuthentication(token);
    handler.onUnsuccessfulAuthentication(token, AuthenticationResult.FAILED);
  }

  /**
   * Method description
   *
   *
   * @param loginAttemptLimit
   * @param loginAttemptLimitTimeout
   *
   * @return
   */
  private LoginAttemptHandler createHandler(int loginAttemptLimit,
    long loginAttemptLimitTimeout)
  {
    ScmConfiguration configuration = new ScmConfiguration();

    configuration.setLoginAttemptLimit(loginAttemptLimit);
    configuration.setLoginAttemptLimitTimeout(loginAttemptLimitTimeout);

    return new ConfigurableLoginAttemptHandler(configuration);
  }
}