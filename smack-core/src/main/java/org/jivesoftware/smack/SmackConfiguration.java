/**
 *
 * Copyright 2003-2007 Jive Software.
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

package org.jivesoftware.smack;

import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;

import org.jivesoftware.smack.compression.XMPPInputOutputStream;
import org.jivesoftware.smack.debugger.ReflectionDebuggerFactory;
import org.jivesoftware.smack.debugger.SmackDebugger;
import org.jivesoftware.smack.debugger.SmackDebuggerFactory;
import org.jivesoftware.smack.parsing.ExceptionThrowingCallback;
import org.jivesoftware.smack.parsing.ParsingExceptionCallback;
import org.jivesoftware.smack.util.Objects;

/**
 * Represents the configuration of Smack. The configuration is used for:
 * <ul>
 *      <li> Initializing classes by loading them at start-up.
 *      <li> Getting the current Smack version.
 *      <li> Getting and setting global library behavior, such as the period of time
 *          to wait for replies to packets from the server. Note: setting these values
 *          via the API will override settings in the configuration file.
 * </ul>
 *
 * Configuration settings are stored in org.jivesoftware.smack/smack-config.xml.
 * 
 * @author Gaston Dombiak
 */
public final class SmackConfiguration {

    private static int defaultPacketReplyTimeout = 5000;
    private static int packetCollectorSize = 5000;

    private static List<String> defaultMechs = new ArrayList<>();

    static Set<String> disabledSmackClasses = new HashSet<>();

    final static List<XMPPInputOutputStream> compressionHandlers = new ArrayList<>(2);

    static boolean smackInitialized = false;

    private static SmackDebuggerFactory debuggerFactory = new ReflectionDebuggerFactory();

    /**
     * Value that indicates whether debugging is enabled. When enabled, a debug
     * window will appear for each new connection that will contain the following
     * information:<ul>
     * <li> Client Traffic -- raw XML traffic generated by Smack and sent to the server.
     * <li> Server Traffic -- raw XML traffic sent by the server to the client.
     * <li> Interpreted Packets -- shows XML packets from the server as parsed by Smack.
     * </ul>
     * Debugging can be enabled by setting this field to true, or by setting the Java system
     * property <tt>smack.debugEnabled</tt> to true. The system property can be set on the
     * command line such as "java SomeApp -Dsmack.debugEnabled=true".
     */
    public static boolean DEBUG = false;

    /**
     * The default parsing exception callback is {@link ExceptionThrowingCallback} which will
     * throw an exception and therefore disconnect the active connection.
     */
    private static ParsingExceptionCallback defaultCallback = new ExceptionThrowingCallback();

    private static HostnameVerifier defaultHostnameVerififer;

    /**
     * Returns the Smack version information, eg "1.3.0".
     * 
     * @return the Smack version information.
     */
    public static String getVersion() {
        return SmackInitialization.SMACK_VERSION;
    }

    /**
     * Returns the number of milliseconds to wait for a response from
     * the server. The default value is 5000 ms.
     * 
     * @return the milliseconds to wait for a response from the server
     * @deprecated use {@link #getDefaultReplyTimeout()} instead.
     */
    @Deprecated
    public static int getDefaultPacketReplyTimeout() {
        return getDefaultReplyTimeout();
    }

    /**
     * Sets the number of milliseconds to wait for a response from
     * the server.
     * 
     * @param timeout the milliseconds to wait for a response from the server
     * @deprecated use {@link #setDefaultReplyTimeout(int)} instead.
     */
    @Deprecated
    public static void setDefaultPacketReplyTimeout(int timeout) {
        setDefaultReplyTimeout(timeout);
    }

    /**
     * Returns the number of milliseconds to wait for a response from
     * the server. The default value is 5000 ms.
     * 
     * @return the milliseconds to wait for a response from the server
     */
    public static int getDefaultReplyTimeout() {
        // The timeout value must be greater than 0 otherwise we will answer the default value
        if (defaultPacketReplyTimeout <= 0) {
            defaultPacketReplyTimeout = 5000;
        }
        return defaultPacketReplyTimeout;
    }

    /**
     * Sets the number of milliseconds to wait for a response from
     * the server.
     * 
     * @param timeout the milliseconds to wait for a response from the server
     */
    public static void setDefaultReplyTimeout(int timeout) {
        if (timeout <= 0) {
            throw new IllegalArgumentException();
        }
        defaultPacketReplyTimeout = timeout;
    }

    /**
     * Gets the default max size of a stanza(/packet) collector before it will delete 
     * the older packets.
     * 
     * @return The number of packets to queue before deleting older packets.
     */
    public static int getStanzaCollectorSize() {
        return packetCollectorSize;
    }

    /**
     * Sets the default max size of a stanza(/packet) collector before it will delete 
     * the older packets.
     * 
     * @param collectorSize the number of packets to queue before deleting older packets.
     */
    public static void setStanzaCollectorSize(int collectorSize) {
        packetCollectorSize = collectorSize;
    }

    /**
     * Add a SASL mechanism to the list to be used.
     *
     * @param mech the SASL mechanism to be added
     */
    public static void addSaslMech(String mech) {
        if (!defaultMechs.contains(mech)) {
            defaultMechs.add(mech);
        }
    }

    /**
     * Add a Collection of SASL mechanisms to the list to be used.
     *
     * @param mechs the Collection of SASL mechanisms to be added
     */
    public static void addSaslMechs(Collection<String> mechs) {
        for (String mech : mechs) {
            addSaslMech(mech);
        }
    }

    /**
     * Sets Smack debugger factory.
     *
     * @param debuggerFactory new debugger factory implementation to be used by Smack
     */
    public static void setDebuggerFactory(SmackDebuggerFactory debuggerFactory) {
        SmackConfiguration.debuggerFactory = debuggerFactory;
    }

    /**
     * Get the debugger factory.
     *
     * @return a debugger factory or <code>null</code>
     */
    public static SmackDebuggerFactory getDebuggerFactory() {
        return debuggerFactory;
    }

    /**
     * Creates new debugger instance with given arguments as parameters. May
     * return <code>null</code> if no DebuggerFactory is set or if the factory
     * did not produce a debugger.
     * 
     * @param connection
     * @param writer
     * @param reader
     * @return a new debugger or <code>null</code>
     */
    public static SmackDebugger createDebugger(XMPPConnection connection, Writer writer, Reader reader) {
        SmackDebuggerFactory factory = getDebuggerFactory();
        if (factory == null) {
            return null;
        } else {
            return factory.create(connection, writer, reader);
        }
    }

    /**
     * Remove a SASL mechanism from the list to be used.
     *
     * @param mech the SASL mechanism to be removed
     */
    public static void removeSaslMech(String mech) {
        defaultMechs.remove(mech);
    }

    /**
     * Remove a Collection of SASL mechanisms to the list to be used.
     *
     * @param mechs the Collection of SASL mechanisms to be removed
     */
    public static void removeSaslMechs(Collection<String> mechs) {
        defaultMechs.removeAll(mechs);
    }

    /**
     * Returns the list of SASL mechanisms to be used. If a SASL mechanism is
     * listed here it does not guarantee it will be used. The server may not
     * support it, or it may not be implemented.
     *
     * @return the list of SASL mechanisms to be used.
     */
    public static List<String> getSaslMechs() {
        return Collections.unmodifiableList(defaultMechs);
    }

    /**
     * Set the default parsing exception callback for all newly created connections.
     *
     * @param callback
     * @see ParsingExceptionCallback
     */
    public static void setDefaultParsingExceptionCallback(ParsingExceptionCallback callback) {
        defaultCallback = callback;
    }

    /**
     * Returns the default parsing exception callback.
     * 
     * @return the default parsing exception callback
     * @see ParsingExceptionCallback
     */
    public static ParsingExceptionCallback getDefaultParsingExceptionCallback() {
        return defaultCallback;
    }

    public static void addCompressionHandler(XMPPInputOutputStream xmppInputOutputStream) {
        compressionHandlers.add(xmppInputOutputStream);
    }

    /**
     * Get compression handlers.
     *
     * @deprecated use {@link #getCompressionHandlers()} instead.
     */
    @Deprecated
    public static List<XMPPInputOutputStream> getCompresionHandlers() {
        return getCompressionHandlers();
    }

    public static List<XMPPInputOutputStream> getCompressionHandlers() {
        List<XMPPInputOutputStream> res = new ArrayList<>(compressionHandlers.size());
        for (XMPPInputOutputStream ios : compressionHandlers) {
            if (ios.isSupported()) {
                res.add(ios);
            }
        }
        return res;
    }

    /**
     * Set the default HostnameVerifier that will be used by XMPP connections to verify the hostname
     * of a TLS certificate. XMPP connections are able to overwrite this settings by supplying a
     * HostnameVerifier in their ConnectionConfiguration with
     * {@link ConnectionConfiguration.Builder#setHostnameVerifier(HostnameVerifier)}.
     *
     * @param verifier HostnameVerifier
     */
    public static void setDefaultHostnameVerifier(HostnameVerifier verifier) {
        defaultHostnameVerififer = verifier;
    }

    /**
     * Convenience method for {@link #addDisabledSmackClass(String)}.
     *
     * @param clz the Smack class to disable
     */
    public static void addDisabledSmackClass(Class<?> clz) {
        addDisabledSmackClass(clz.getName());
    }

    /**
     * Add a class to the disabled smack classes.
     * <p>
     * {@code className} can also be a package name, in this case, the entire
     * package is disabled (but can be manually enabled).
     * </p>
     *
     * @param className
     */
    public static void addDisabledSmackClass(String className) {
        disabledSmackClasses.add(className);
    }

    /**
     * Add the given class names to the list of disabled Smack classes.
     *
     * @param classNames the Smack classes to disable.
     * @see #addDisabledSmackClass(String)
     */
    public static void addDisabledSmackClasses(String... classNames) {
        for (String className : classNames) {
            addDisabledSmackClass(className);
        }
    }

    public static boolean isDisabledSmackClass(String className) {
        for (String disabledClassOrPackage : disabledSmackClasses) {
            if (disabledClassOrPackage.equals(className)) {
                return true;
            }
            int lastDotIndex = disabledClassOrPackage.lastIndexOf('.');
            // Security check to avoid NPEs if someone entered 'foo.bar.'
            if (disabledClassOrPackage.length() > lastDotIndex
                            // disabledClassOrPackage is not an Class
                            && !Character.isUpperCase(disabledClassOrPackage.charAt(lastDotIndex + 1))
                            // classToLoad startsWith the package disabledClassOrPackage disables
                            && className.startsWith(disabledClassOrPackage)) {
                // Skip the class because the whole package was disabled
                return true;
            }
        }
        return false;
    }

    /**
     * Check if Smack was successfully initialized.
     * 
     * @return true if smack was initialized, false otherwise
     */
    public static boolean isSmackInitialized() {
        return smackInitialized;
    }

    /**
     * Get the default HostnameVerifier
     *
     * @return the default HostnameVerifier or <code>null</code> if none was set
     */
    static HostnameVerifier getDefaultHostnameVerifier() {
        return defaultHostnameVerififer;
    }

    public enum UnknownIqRequestReplyMode {
        doNotReply,
        replyFeatureNotImplemented,
        replyServiceUnavailable,
    }

    // TODO Change to replyFeatureNotImplemented in Smack 4.3
    private static UnknownIqRequestReplyMode unknownIqRequestReplyMode = UnknownIqRequestReplyMode.replyServiceUnavailable;

    public static UnknownIqRequestReplyMode getUnknownIqRequestReplyMode() {
        return unknownIqRequestReplyMode;
    }

    public static void setUnknownIqRequestReplyMode(UnknownIqRequestReplyMode unknownIqRequestReplyMode) {
        SmackConfiguration.unknownIqRequestReplyMode = Objects.requireNonNull(unknownIqRequestReplyMode, "Must set mode");
    }
}
