/**
 * Copyright 2005-2015 Red Hat, Inc.
 * <p/>
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package io.fabric8.forge.camel.commands.project;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.fabric8.forge.camel.commands.project.dto.ComponentDto;
import io.fabric8.forge.camel.commands.project.dto.OutputFormat;
import io.fabric8.forge.camel.commands.project.helper.CamelCommandsHelper;
import io.fabric8.forge.camel.commands.project.helper.OutputFormatHelper;
import io.fabric8.utils.TablePrinter;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.dependencies.DependencyInstaller;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

import javax.inject.Inject;
import java.util.concurrent.Callable;

public class CamelGetAvailableComponentsCommand extends AbstractCamelProjectCommand {

    @Inject
    @WithAttributes(label = "Filter", required = false, description = "To filter components")
    private UISelectOne<String> filter;

    @Inject
    @WithAttributes(label = "Name", defaultValue = "Text", description = "Name of dataformat to add")
    private UISelectOne<OutputFormat> format;

    @Inject
    @WithAttributes(label = "Verbose", defaultValue = "false", description = "Whether to use verbose text output")
    private UIInput<Boolean> verbose;

    @Inject
    private DependencyInstaller dependencyInstaller;

    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.forCommand(CamelGetAvailableComponentsCommand.class).name(
                "Camel: Get Available Components").category(Categories.create(CATEGORY))
                .description("Gets the available components that could be added to the current project");
    }

    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        Project project = getSelectedProject(builder);

        filter.setValueChoices(CamelCommandsHelper.createComponentLabelValues(project, getCamelCatalog()));
        filter.setDefaultValue("<all>");

        builder.add(filter).add(format).add(verbose);

    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        Project project = getSelectedProject(context);
        Callable<Iterable<ComponentDto>> callable = CamelCommandsHelper.createComponentDtoValues(project, getCamelCatalog(), filter, true);
        Iterable<ComponentDto> results = callable.call();
        String result = formatResult(results);
        return Results.success(result);
    }

    protected String formatResult(Iterable<ComponentDto> results) throws JsonProcessingException {
        OutputFormat outputFormat = format.getValue();
        switch (outputFormat) {
            case JSON:
                return OutputFormatHelper.toJson(results);
            default:
                return textResult(results);
        }
    }

    protected String textResult(Iterable<ComponentDto> components) {
        StringBuilder buffer = new StringBuilder();

        TablePrinter table = new TablePrinter();

        Boolean verboseValue = verbose.getValue();
        if (verboseValue != null && verboseValue.booleanValue()) {
            table.columns("name", "description", "tags", "syntax", "artifact");
            for (ComponentDto component : components) {
                table.row(component.getScheme(), component.getDescription(), component.getLabel(), component.getSyntax(),
                        component.getGroupId() + ":" + component.getArtifactId() + ":" + component.getVersion());
            }
        } else {
            table.columns("name", "description");
            for (ComponentDto component : components) {
                table.row(component.getScheme(), component.getDescription());
            }
        }
        OutputFormatHelper.addTableTextOutput(buffer, "Components", table);
        return buffer.toString();
    }

}
