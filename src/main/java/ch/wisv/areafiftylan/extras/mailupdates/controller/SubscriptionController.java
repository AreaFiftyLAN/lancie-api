/*
 *  Copyright (c) 2018  W.I.S.V. 'Christiaan Huygens'
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

import static ch.wisv.areafiftylan.utils.ResponseEntityBuilder.createResponseEntity;

import ch.wisv.areafiftylan.exception.SubscriptionNotFoundException;
import ch.wisv.areafiftylan.extras.mailupdates.model.Subscription;
import ch.wisv.areafiftylan.extras.mailupdates.model.SubscriptionDTO;
import ch.wisv.areafiftylan.extras.mailupdates.service.SubscriptionService;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

  @DeleteMapping("/{id}")
  public ResponseEntity<?> removeSubscription(@PathVariable Long id) {
    subscriptionService.removeSubscription(id);
    return createResponseEntity(HttpStatus.OK,
        "Successfully removed the subscription with ID: " + id);
  }

  @DeleteMapping
  @PreAuthorize("hasRole('COMMITTEE')")
  public ResponseEntity<?> removeAllSubscription() {
    subscriptionService.removeAllSubscriptions();
    return createResponseEntity(HttpStatus.OK,
        "Successfully removed all subscriptions");
  }

  @ExceptionHandler(SubscriptionNotFoundException.class)
  public ResponseEntity<?> handleSubscriptionNotFoundException(SubscriptionNotFoundException ex) {
    return createResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<?> handleDataIntegrityViolationException(
      DataIntegrityViolationException ex) {
    return createResponseEntity(HttpStatus.CONFLICT,
        "You have already subscribed with that email address!");
  }

}
