/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.techdev;

import org.springframework.social.wunderlist.api.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Alexander Hanschke
 */
@Controller
public class ShowcaseController {

    private final Wunderlist wunderlist;

    @Inject
    public ShowcaseController(Wunderlist wunderlist) {
        this.wunderlist = wunderlist;
    }

    @RequestMapping("/")
    public String home(Model model) {
        WunderlistUser user = wunderlist.userOperations().getUser();
        List<WunderlistList> lists = wunderlist.listOperations().getLists();

        model.addAttribute("user", user);
        model.addAttribute("lists", lists);

        return "home";
    }

    @RequestMapping("/lists/{id}")
    public String list(Model model, @PathVariable long id) {
        WunderlistList list = wunderlist.listOperations().getList(id);
        List<WunderlistTask> tasks = wunderlist.taskOperations().getTasks(list.getId());
        WunderlistTasksCount count = wunderlist.listOperations().getTasksCount(list.getId());

        model.addAttribute("list", list);
        model.addAttribute("count", count);
        model.addAttribute("tasks", tasks);

        return "list";
    }
}
