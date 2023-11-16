package com.test.caller.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AfterStartUp {

    @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterStartUp() {
    }
}
