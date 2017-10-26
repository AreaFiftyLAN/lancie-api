/*
 *  Copyright (c) 2017  W.I.S.V. 'Christiaan Huygens'
 *
 *      This program is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      This program is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.wisv.areafiftylan.extras.mailupdates.controller;

import ch.wisv.areafiftylan.exception.SubscriptionNotFoundException;
import ch.wisv.areafiftylan.extras.mailupdates.model.Subscription;
import ch.wisv.areafiftylan.extras.mailupdates.model.SubscriptionDTO;
import ch.wisv.areafiftylan.extras.mailupdates.service.SubscriptionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import java.util.Collection;
import java.util.Set;

import static ch.wisv.areafiftylan.utils.ResponseEntityBuilder.createResponseEntity;

/**
 * @author Jurriaan Den Toonder Created on 23-10-17
 */
@RestController
@RequestMapping("/subscriptions")
public class SubscriptionController {
  private final SubscriptionService subscriptionService;

  @Autowired
  public SubscriptionController(SubscriptionService subscriptionService) {
    this.subscriptionService = subscriptionService;
  }

  @GetMapping
  @PreAuthorize("hasRole('COMMITTEE')")
  public Collection<Subscription> getAllSubscriptions() {
    return subscriptionService.getSubscriptions();
  }

  @PostMapping
  public ResponseEntity<?> addSubscription(@RequestBody SubscriptionDTO subscriptionDTO) {
    String email = subscriptionDTO.getEmail();
    subscriptionService.addSubscription(email);
    return createResponseEntity(HttpStatus.OK,
        "Successfully added " + email + " to the subscriptions list.");
  }

  @DeleteMapping
  public ResponseEntity<?> removeSubscription(@RequestBody SubscriptionDTO subscriptionDTO) {
    String email = subscriptionDTO.getEmail();
    subscriptionService.removeSubscription(email);
    return createResponseEntity(HttpStatus.OK,
        "Successfully removed the subscription with email: " + email);
  }

  @ExceptionHandler(SubscriptionNotFoundException.class)
  public ResponseEntity<?> handleSubscriptionNotFoundException(SubscriptionNotFoundException ex) {
    return createResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException ex) {
    Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
    ConstraintViolation<?> violation = violations.iterator().next();
    return createResponseEntity(HttpStatus.BAD_REQUEST, violation.getMessage());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<?> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
    return createResponseEntity(HttpStatus.BAD_REQUEST, "You have already subscribed with that email address!");
  }

}
