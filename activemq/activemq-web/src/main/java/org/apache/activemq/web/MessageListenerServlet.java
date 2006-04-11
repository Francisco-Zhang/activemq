/**
 *
 * Copyright 2005-2006 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.activemq.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.activemq.MessageAvailableConsumer;
import org.apache.activemq.MessageAvailableListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.util.ajax.Continuation;
import org.mortbay.util.ajax.ContinuationSupport;

/**
 * A servlet for sending and receiving messages to/from JMS destinations using
 * HTTP POST for sending and HTTP GET for receiving. <p/> You can specify the
 * destination and whether it is a topic or queue via configuration details on
 * the servlet or as request parameters. <p/> For reading messages you can
 * specify a readTimeout parameter to determine how long the servlet should
 * block for.
 * 
 * The servlet can be configured with the following init parameters:<dl>
 * <dt>defaultReadTimeout</dt><dd>The default time in ms to wait for messages. 
 * May be overridden by a request using the 'timeout' parameter</dd>
 * <dt>maximumReadTimeout</dt><dd>The maximum value a request may specify for the 'timeout' parameter</dd>
 * <dt>maximumMessages</dt><dd>maximum messages to send per response</dd>
 * <dt></dt><dd></dd>
 * </dl>
 *  
 * 
 * @version $Revision: 1.1.1.1 $
 */
public class MessageListenerServlet extends MessageServletSupport {
    private static final Log log = LogFactory.getLog(MessageListenerServlet.class);

    private String readTimeoutParameter = "timeout";

    private long defaultReadTimeout = -1;

    private long maximumReadTimeout = 25000;

    private int maximumMessages = 100;
    
    public void init() throws ServletException {
        ServletConfig servletConfig = getServletConfig();
        String name = servletConfig.getInitParameter("defaultReadTimeout");
        if (name != null) {
            defaultReadTimeout = asLong(name);
        }
        name = servletConfig.getInitParameter("maximumReadTimeout");
        if (name != null) {
            maximumReadTimeout = asLong(name);
        }
        name = servletConfig.getInitParameter("maximumMessages");
        if (name != null) {
            maximumMessages = (int) asLong(name);
        }
    }

    /**
     * Sends a message to a destination
     * 
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        // lets turn the HTTP post into a JMS Message
        
        WebClient client = getWebClient(request);
        String message_ids="";
        
        synchronized (client) {
            
            if (log.isDebugEnabled()) {
                log.debug("POST client="+client+" session="+request.getSession().getId()+" info="+request.getPathInfo()+" contentType="+request.getContentType());
            // dump(request.getParameterMap());
            }
            String[] destinations = request.getParameterValues("destination");
            String[] messages = request.getParameterValues("message");
            String[] types = request.getParameterValues("type");
            
            if (destinations.length!=messages.length || messages.length!=types.length)
            {
                if (log.isDebugEnabled()) {
                    log.debug("ERROR destination="+destinations.length+" message="+messages.length+" type="+types.length);
                }
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,"missmatched destination, message or type");
                return;
            }
            
            for (int i=0;i<types.length;i++)
            {
                try {
                    String type=types[i];
                    Destination destination=getDestination(client,request,destinations[i]);
                    
                    if (log.isDebugEnabled()) {
                        log.debug(i+" destination="+destinations[i]+" message="+messages[i]+" type="+types[i]);
                        log.debug(destination+" is a "+destination.getClass().getName());
                    }
                    
                    if ("listen".equals(type))
                    {
                        Listener listener = getListener(request);
                        Map consumerIdMap = getConsumerIdMap(request);
                        client.closeConsumer(destination); // drop any existing consumer.
                        MessageAvailableConsumer consumer = (MessageAvailableConsumer) client.getConsumer(destination);
                        
                        consumer.setAvailableListener(listener);
                        consumerIdMap.put(consumer, messages[i]);
                        if (log.isDebugEnabled()) {
                            log.debug("Subscribed: "+consumer+" to "+destination+" id="+messages[i]);
                        }
                    }
                    else if ("unlisten".equals(type))
                    {
                        Map consumerIdMap = getConsumerIdMap(request);
                        MessageAvailableConsumer consumer = (MessageAvailableConsumer) client.getConsumer(destination);
                        
                        consumer.setAvailableListener(null);
                        consumerIdMap.remove(consumer);
                        client.closeConsumer(destination);
                        if (log.isDebugEnabled()) {
                            log.debug("Unsubscribed: "+consumer);
                        }
                    }
                    else if ("send".equals(type))
                    {
                        TextMessage message = client.getSession().createTextMessage(messages[i]);
                        // TODO sent message parameters
                        client.send(destination, message);
                        message_ids+=message.getJMSMessageID()+"\n";
                        if (log.isDebugEnabled()) {
                            log.debug("Sent "+messages[i]+" to "+destination);
                        }
                    }
                    else
                        log.warn("unknown type "+type);
                    
                } 
                catch (JMSException e) {
                    log.warn("jms", e);
                }
            }
        }
            
        if ("true".equals(request.getParameter("poll")))
        {
            try
            {
                // TODO return message IDs
                doMessages(client,request,response);
            }
            catch (JMSException e) 
            {
                throw new ServletException("JMS problem: " + e, e);
            }
        }
        else 
        {
            // handle simple POST of a message
            if (request.getContentLength()!=0 && 
               (request.getContentType()==null || !request.getContentType().toLowerCase().startsWith("application/x-www-form-urlencoded")))
            {
                try {
                    Destination destination=getDestination(client, request);
                    String body = getPostedMessageBody(request);
                    TextMessage message = client.getSession().createTextMessage(body );
                    client.send(destination, message);
                    if (log.isDebugEnabled()) {
                        log.debug("Sent to destination: " + destination + " body: " + body);
                    }
                    message_ids+=message.getJMSMessageID()+"\n";
                }
                catch (JMSException e) {
                    throw new ServletException(e);
                }
            }
            
            response.setContentType("text/plain");
            response.setHeader("Cache-Control", "no-cache");
            response.getWriter().print(message_ids);
        }
    }

    /**
     * Supports a HTTP DELETE to be equivlanent of consuming a singe message
     * from a queue
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            WebClient client = getWebClient(request);
            if (log.isDebugEnabled()) {
                log.debug("GET client="+client+" session="+request.getSession().getId()+" uri="+request.getRequestURI()+" query="+request.getQueryString());
            }
            
            doMessages(client, request, response);
        }
        catch (JMSException e) {
            throw new ServletException("JMS problem: " + e, e);
        }
    }


    /**
     * Reads a message from a destination up to some specific timeout period
     * 
     * @param client
     *            The webclient
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void doMessages(WebClient client, HttpServletRequest request, HttpServletResponse response) throws JMSException, IOException {

        int messages = 0;
        // This is a poll for any messages

        long timeout = getReadTimeout(request);
        if (log.isDebugEnabled()) {
            log.debug("doMessage timeout="+timeout);
        }
        
        Continuation continuation = ContinuationSupport.getContinuation(request, client);
        Listener listener = getListener(request);
        if (listener!=null && continuation!=null && !continuation.isPending())
            listener.access();

        Message message = null;
        synchronized (client) {

            List consumers = client.getConsumers();
            MessageAvailableConsumer consumer = null;

            // Look for a message that is ready to go
            for (int i = 0; message == null && i < consumers.size(); i++) {
                consumer = (MessageAvailableConsumer) consumers.get(i);
                if (consumer.getAvailableListener() == null)
                    continue;

                // Look for any available messages
                message = consumer.receiveNoWait();
                if (log.isDebugEnabled()) {
                    log.debug("received "+message+" from "+consumer);
                }
            }

            // Get an existing Continuation or create a new one if there are no
            // messages

            if (message == null) {
                // register this continuation with our listener.
                listener.setContinuation(continuation);

                // Get the continuation object (may wait and/or retry
                // request here).
                continuation.suspend(timeout);
            }
            listener.setContinuation(null);

            // prepare the responds
            response.setContentType("text/xml");
            response.setHeader("Cache-Control", "no-cache");

            StringWriter swriter = new StringWriter();
            PrintWriter writer = new PrintWriter(swriter);

            Map consumerIdMap = getConsumerIdMap(request);
            response.setStatus(HttpServletResponse.SC_OK);
            writer.println("<ajax-response>");

            // Send any message we already have
            if (message != null) {
                String id = (String) consumerIdMap.get(consumer);
                writer.print("<response id='");
                writer.print(id);
                writer.print("'>");
                writeMessageResponse(writer, message);
                writer.println("</response>");
                messages++;
            }

            // Send the rest of the messages
            for (int i = 0; i < consumers.size() && messages < maximumMessages; i++) {
                consumer = (MessageAvailableConsumer) consumers.get(i);
                if (consumer.getAvailableListener() == null)
                    continue;

                // Look for any available messages
                message = consumer.receiveNoWait();
                while (message != null && messages < maximumMessages) {
                    String id = (String) consumerIdMap.get(consumer);
                    writer.print("<response id='");
                    writer.print(id);
                    writer.print("'>");
                    writeMessageResponse(writer, message);
                    writer.println("</response>");
                    messages++;
                    message = consumer.receiveNoWait();
                }
            }

            // Add poll message
            // writer.println("<response type='object' id='amqPoll'><ok/></response>");
            
            writer.print("</ajax-response>");

            writer.flush();
            String m = swriter.toString();
            // System.err.println(m);
            response.getWriter().println(m);
        }

    }

    protected void writeMessageResponse(PrintWriter writer, Message message) throws JMSException, IOException {
        if (message instanceof TextMessage) {
            TextMessage textMsg = (TextMessage) message;
            String txt = textMsg.getText();
            if (txt.startsWith("<?")) {
                txt = txt.substring(txt.indexOf("?>") + 2);
            }
            writer.print(txt);
        } else if (message instanceof ObjectMessage) {
            ObjectMessage objectMsg = (ObjectMessage) message;
            Object object = objectMsg.getObject();
            writer.print(object.toString());
        }
    }

    protected Listener getListener(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Listener listener = (Listener) session.getAttribute("mls.listener");
        if (listener == null) {
            listener = new Listener(getWebClient(request));
            session.setAttribute("mls.listener", listener);
        }
        return listener;
    }

    protected Map getConsumerIdMap(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Map map = (Map) session.getAttribute("mls.consumerIdMap");
        if (map == null) {
            map = new HashMap();
            session.setAttribute("mls.consumerIdMap", map);
        }
        return map;
    }

    protected boolean isRicoAjax(HttpServletRequest request) {
        String rico = request.getParameter("rico");
        return rico != null && rico.equals("true");
    }

    /**
     * @return the timeout value for read requests which is always >= 0 and <=
     *         maximumReadTimeout to avoid DoS attacks
     */
    protected long getReadTimeout(HttpServletRequest request) {
        long answer = defaultReadTimeout;

        String name = request.getParameter(readTimeoutParameter);
        if (name != null) {
            answer = asLong(name);
        }
        if (answer < 0 || answer > maximumReadTimeout) {
            answer = maximumReadTimeout;
        }
        return answer;
    }

    /*
     * Listen for available messages and wakeup any continuations.
     */
    private class Listener implements MessageAvailableListener {
        WebClient client;
        long lastAccess;
        Continuation continuation;

        Listener(WebClient client) {
            this.client = client;
        }

        public void access()
        {
            lastAccess=System.currentTimeMillis();
        }
        
        public void setContinuation(Continuation continuation) {
            synchronized (client) {
                this.continuation = continuation;
            }
        }

        public void onMessageAvailable(MessageConsumer consumer) {
            synchronized (client) {
                if (log.isDebugEnabled()) {
                    log.debug("message for "+consumer+"continuation="+continuation);
                }
                if (continuation != null)
                    continuation.resume();
                else if (System.currentTimeMillis()-lastAccess>2*maximumReadTimeout)
                {
                    client.closeConsumers();
                }
                continuation = null;
            }
        }

    }
}
