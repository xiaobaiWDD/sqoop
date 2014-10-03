/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sqoop.repository.derby;

import org.apache.sqoop.model.MConnector;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Test connector methods on Derby repository.
 */
public class TestConnectorHandling extends DerbyTestCase {

  DerbyRepositoryHandler handler;

  @Before
  public void setUp() throws Exception {
    super.setUp();

    handler = new DerbyRepositoryHandler();

    // We always needs schema for this test case
    createSchema();
  }

  @Test
  public void testFindConnector() throws Exception {
    // On empty repository, no connectors should be there
    assertNull(handler.findConnector("A", getDerbyDatabaseConnection()));
    assertNull(handler.findConnector("B", getDerbyDatabaseConnection()));

    // Load connector into repository
    loadConnectorLinkConfig();

    // Retrieve it
    MConnector connector = handler.findConnector("A", getDerbyDatabaseConnection());
    assertNotNull(connector);

    // Get original structure
    MConnector original = getConnector();

    // And compare them
    assertEquals(original, connector);
  }

  @Test
  public void testFindAllConnectors() throws Exception {
    // No connectors in an empty repository, we expect an empty list
    assertEquals(handler.findConnectors(getDerbyDatabaseConnection()).size(),0);

    loadConnectorLinkConfig();
    addConnector();

    // Retrieve connectors
    List<MConnector> connectors = handler.findConnectors(getDerbyDatabaseConnection());
    assertNotNull(connectors);
    assertEquals(connectors.size(),2);
    assertEquals(connectors.get(0).getUniqueName(),"A");
    assertEquals(connectors.get(1).getUniqueName(),"B");


  }

  @Test
  public void testRegisterConnector() throws Exception {
    MConnector connector = getConnector();

    handler.registerConnector(connector, getDerbyDatabaseConnection());

    // Connector should get persistence ID
    assertEquals(1, connector.getPersistenceId());

    // Now check content in corresponding tables
    assertCountForTable("SQOOP.SQ_CONNECTOR", 1);
    assertCountForTable("SQOOP.SQ_CONFIG", 6);
    assertCountForTable("SQOOP.SQ_INPUT", 12);

    // Registered connector should be easily recovered back
    MConnector retrieved = handler.findConnector("A", getDerbyDatabaseConnection());
    assertNotNull(retrieved);
    assertEquals(connector, retrieved);
  }
}