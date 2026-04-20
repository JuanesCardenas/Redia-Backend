package com.redia.back.service.impl;

import com.redia.back.dto.UserResponseDTO;
import com.redia.back.exception.BadRequestException;
import com.redia.back.model.User;
import com.redia.back.repository.UserRepository;
import com.redia.back.repository.ReservationRepository;
import com.redia.back.repository.OrderRepository;
import com.redia.back.model.Reservation;
import com.redia.back.model.Order;
import com.redia.back.service.UserAdminService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación del servicio administrativo de usuarios.
 */
@Service
@Transactional
public class UserAdminServiceImpl implements UserAdminService {

        private final UserRepository userRepository;
        private final ReservationRepository reservationRepository;
        private final OrderRepository orderRepository;

        public UserAdminServiceImpl(
                        UserRepository userRepository, 
                        ReservationRepository reservationRepository,
                        OrderRepository orderRepository) {
                this.userRepository = userRepository;
                this.reservationRepository = reservationRepository;
                this.orderRepository = orderRepository;
        }

        /**
         * Obtiene todos los usuarios registrados en el sistema
         * y los transforma a DTO para evitar exponer datos sensibles.
         */
        @Override
        public List<UserResponseDTO> getAllUsers() {

                return userRepository.findAll()
                                .stream()
                                .filter(User::isActivo)
                                .map(user -> new UserResponseDTO(
                                                user.getId(),
                                                user.getNombre(),
                                                user.getEmail(),
                                                user.getTelefono(),
                                                user.getRole(),
                                                user.getFotoUrl(),
                                                user.isBajaSolicitada()))
                                .toList();
        }

        /**
         * Elimina definitivamente un usuario por su id (hard delete).
         */
        @Override
        public void deleteUser(String userId) {

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new BadRequestException("Usuario no encontrado."));

                // 1. Encontrar todas las reservas de este cliente
                List<Reservation> userReservations = reservationRepository.findByClienteId(userId);

                // 2. Por cada reserva, destruir los pedidos atados a ella para romper la llave foránea
                for (Reservation r : userReservations) {
                        List<Order> orders = orderRepository.findByReservationId(r.getId());
                        orderRepository.deleteAll(orders);
                }

                // 3. Destruir las reservas
                reservationRepository.deleteAll(userReservations);

                // 4. Finalmente, ejecutar el Hard Delete SQL crudo del usuario
                userRepository.delete(user);
        }
}