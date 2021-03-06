/*
 * Licensed to Crate.IO GmbH ("Crate") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.  Crate licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial agreement.
 */

package io.crate.analyze;

import io.crate.analyze.expressions.ExpressionToStringVisitor;
import io.crate.exceptions.NoPermissionException;
import io.crate.sql.tree.KillStatement;
import io.crate.sql.tree.Node;
import org.elasticsearch.authentication.AuthResult;
import org.elasticsearch.authentication.AuthService;
import org.elasticsearch.authentication.VirtualTableNames;
import org.elasticsearch.cluster.metadata.PrivilegeType;
import org.elasticsearch.rest.RestStatus;

import java.util.UUID;

public class KillAnalyzer {

    private KillAnalyzer() {}


    public static KillAnalyzedStatement analyze(KillStatement killStatement, ParameterContext parameterContext) {

        // Add SQL Authentication
        // GaoPan 2016/06/16
        AuthResult authResult = AuthService.sqlAuthenticate(parameterContext.getLoginUserContext(),
                VirtualTableNames.sys.name(), VirtualTableNames.jobs.name(), PrivilegeType.READ_WRITE);
        if (authResult.getStatus() != RestStatus.OK) {
            throw new NoPermissionException(authResult.getStatus().getStatus(), authResult.getMessage());
        }

        if (killStatement.jobId().isPresent()) {
            UUID jobId;
            try {
                jobId = UUID.fromString(ExpressionToStringVisitor
                        .convert(killStatement.jobId().get(), parameterContext.parameters()));
            } catch (Exception e) {
                throw new IllegalArgumentException("Can not parse job ID", e);
            }
            return new KillAnalyzedStatement(jobId);
        }
        return new KillAnalyzedStatement();
    }
}
