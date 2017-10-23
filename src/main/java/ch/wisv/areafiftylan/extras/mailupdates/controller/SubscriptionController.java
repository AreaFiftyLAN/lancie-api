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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

import javax.websocket.server.PathParam;

import ch.wisv.areafiftylan.exception.SubscriptionNotFoundException;
import ch.wisv.areafiftylan.extras.mailupdates.model.Subscription;
import ch.wisv.areafiftylan.extras.mailupdates.service.SubscriptionService;

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

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('COMMITTEE')")
  public Subscription getSubscriptionById(@PathParam("id") Long id) {
    return subscriptionService.getSubscriptionById(id);
  }

  @PostMapping
  public ResponseEntity<?> addSubscription(@RequestBody String email) {
    subscriptionService.addSubscription(email);
    return createResponseEntity(HttpStatus.OK,
        "Successfully added " + email + " to the subscriptions list.");
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> removeSubscription(@PathParam("id") Long id) {
    subscriptionService.removeSubscription(id);
    return createResponseEntity(HttpStatus.OK,
        "Successfully removed the subscription with id: " + id);
  }

  @ExceptionHandler(SubscriptionNotFoundException.class)
  public ResponseEntity<?> handleSubscriptionNotFoundException(SubscriptionNotFoundException ex) {
    return createResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage());
  }

}
