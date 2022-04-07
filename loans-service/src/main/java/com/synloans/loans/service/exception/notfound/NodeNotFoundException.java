package com.synloans.loans.service.exception.notfound;

import com.synloans.loans.service.exception.notfound.base.NotFoundException;

public class NodeNotFoundException extends NotFoundException {
    public NodeNotFoundException() {
    }

    public NodeNotFoundException(String message) {
        super(message);
    }

    public NodeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NodeNotFoundException(Throwable cause) {
        super(cause);
    }
}
