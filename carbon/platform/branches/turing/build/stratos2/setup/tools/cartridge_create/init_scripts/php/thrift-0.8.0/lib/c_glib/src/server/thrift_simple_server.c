/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

#include "server/thrift_simple_server.h"
#include "transport/thrift_transport_factory.h"
#include "protocol/thrift_protocol_factory.h"
#include "protocol/thrift_binary_protocol_factory.h"

G_DEFINE_TYPE(ThriftSimpleServer, thrift_simple_server, THRIFT_TYPE_SERVER)

void
thrift_simple_server_serve (ThriftServer *server)
{
  g_return_if_fail (THRIFT_IS_SIMPLE_SERVER (server));

  ThriftTransport *t = NULL;
  ThriftTransport *input_transport = NULL, *output_transport = NULL;
  ThriftProtocol *input_protocol = NULL, *output_protocol = NULL;
  ThriftSimpleServer *tss = THRIFT_SIMPLE_SERVER(server);

  THRIFT_SERVER_TRANSPORT_GET_CLASS (server->server_transport)
      ->listen (server->server_transport, NULL);

  tss->running = TRUE;
  while (tss->running == TRUE)
  {
    t = thrift_server_transport_accept (server->server_transport, NULL);
    input_transport =
        THRIFT_TRANSPORT_FACTORY_GET_CLASS (server->input_transport_factory)
            ->get_transport (server->input_transport_factory, t);
    output_transport = 
        THRIFT_TRANSPORT_FACTORY_GET_CLASS (server->output_transport_factory)
            ->get_transport (server->output_transport_factory, t);
    input_protocol =
        THRIFT_PROTOCOL_FACTORY_GET_CLASS (server->input_protocol_factory)
            ->get_protocol (server->input_protocol_factory, t);
    output_protocol =
        THRIFT_PROTOCOL_FACTORY_GET_CLASS (server->output_protocol_factory)
            ->get_protocol (server->output_protocol_factory, t);

    while (THRIFT_PROCESSOR_GET_CLASS (server->processor)
               ->process (server->processor, input_protocol, output_protocol))
    {
      // TODO: implement transport peek ()
    }

    // TODO: handle exceptions
    THRIFT_TRANSPORT_GET_CLASS (input_transport)->close (input_transport, NULL);
    THRIFT_TRANSPORT_GET_CLASS (output_transport)->close (output_transport,
                                                          NULL);
  } 

  // attempt to shutdown
  THRIFT_SERVER_TRANSPORT_GET_CLASS (server->server_transport)
      ->close (server->server_transport, NULL); 
}

void
thrift_simple_server_stop (ThriftServer *server)
{
  g_return_if_fail (THRIFT_IS_SIMPLE_SERVER (server));
  (THRIFT_SIMPLE_SERVER (server))->running = FALSE;
}

static void
thrift_simple_server_init (ThriftSimpleServer *tss)
{
  tss->running = FALSE;

  ThriftServer *server = THRIFT_SERVER(tss);

  if (server->input_transport_factory == NULL)
  {
    server->input_transport_factory =
        g_object_new (THRIFT_TYPE_TRANSPORT_FACTORY, NULL);
  }
  if (server->output_transport_factory == NULL)
  {
    server->output_transport_factory =
        g_object_new (THRIFT_TYPE_TRANSPORT_FACTORY, NULL);
  }
  if (server->input_protocol_factory == NULL)
  {
    server->input_protocol_factory =
        g_object_new (THRIFT_TYPE_BINARY_PROTOCOL_FACTORY, NULL);
  }
  if (server->output_protocol_factory == NULL)
  {
    server->output_protocol_factory =
        g_object_new (THRIFT_TYPE_BINARY_PROTOCOL_FACTORY, NULL);
  }
}

/* initialize the class */
static void
thrift_simple_server_class_init (ThriftSimpleServerClass *class)
{
  ThriftServerClass *cls = THRIFT_SERVER_CLASS(class);

  cls->serve = thrift_simple_server_serve;
  cls->stop = thrift_simple_server_stop;
}
