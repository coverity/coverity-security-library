package com.coverity.security.path;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use the <code>@TrustedPath</code> annotation on dynamic data which should be trusted by Coverity's static analysis,
 * such as paths read from properties files. See the Coverity Security Library
 * <a href="https://github.com/coverity/coverity-security-library/blob/develop/README.md">README</a> for more information.
 *
 * @see SimplePath
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface TrustedPath {
}
