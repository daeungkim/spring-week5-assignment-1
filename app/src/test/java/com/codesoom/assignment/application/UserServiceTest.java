package com.codesoom.assignment.application;

import static com.codesoom.assignment.constants.UserConstants.ID;
import static com.codesoom.assignment.constants.UserConstants.USER;
import static com.codesoom.assignment.constants.UserConstants.USER_DATA;
import static com.codesoom.assignment.constants.UserConstants.UPDATE_USER_DATA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.codesoom.assignment.NotFoundException;
import com.codesoom.assignment.domain.User;
import com.codesoom.assignment.domain.UserRepository;
import com.codesoom.assignment.dto.UserData;
import com.codesoom.assignment.dto.UpdateUserData;
import com.github.dozermapper.core.Mapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;

@Nested
@DisplayName("UserService 클래스")
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Mapper dozerMapper;

    private Mapper verifyMapper(final int invokeCounts) {
        return verify(dozerMapper, times(invokeCounts));
    }

    private OngoingStubbing<Optional<User>> mockFindById() {
        return when(userRepository.findById(anyLong()));
    }

    private OngoingStubbing<User> mockSave() {
        return when(userRepository.save(any(User.class)));
    }

    private UserRepository verifyRepository(final int invokeNumber) {
        return verify(userRepository, times(invokeNumber));
    }

    @Nested
    @DisplayName("createUser 메서드는")
    public class Describe_createUser {
        private OngoingStubbing<User> mockMapper() {
            return when(dozerMapper.map(any(UserData.class), eq(User.class)));
        }

        private User subject() {
            return userService.createUser(USER_DATA);
        }

        @Nested
        @DisplayName("저장하지 않은 User를 생성하고")
        public class Context_mapper_creates_an_unsaved_user {
            @BeforeEach
            private void beforeEach() {
                mockMapper()
                    .thenReturn(USER);
            }

            @AfterEach
            private void afterEach() {
                verifyMapper(1)
                    .map(any(UserData.class), eq(User.class));
            }

            @Nested
            @DisplayName("생성한 User 저장에 성공하면")
            public class Context_repository_saves_user {
                @BeforeEach
                private void beforeEach() {
                    mockSave()
                        .thenReturn(USER);
                }

                @AfterEach
                private void afterEach() {
                    verifyRepository(1)
                        .save(any(User.class));
                }

                @Test
                @DisplayName("저장한 User를 리턴한다.")
                public void it_returns_a_saved_user() {
                    assertThat(subject())
                        .isInstanceOf(User.class);
                }
            }
        }
    }

    @Nested
    @DisplayName("deleteUser 메서드는")
    public class Describe_deleteUser {
        private void subject() {
            userService.deleteUser(ID);
        }

        @Nested
        @DisplayName("삭제할 User를 찾지 못하는 경우")
        public class Context_repository_throws_an_exception {
            @BeforeEach
            private void beforeEach() {
                mockFindById()
                    .thenThrow(new NotFoundException(ID, User.class.getSimpleName()));
            }

            @AfterEach
            private void afterEach() {
                verifyRepository(1)
                    .findById(anyLong());
                verifyRepository(0)
                    .delete(any(User.class));
            }

            @Test
            @DisplayName("NotFoundException을 던진다")
            public void it_throws_a_notFoundException() {
                assertThatThrownBy(() -> subject())
                    .isInstanceOf(NotFoundException.class);
            }
        }

        @Nested
        @DisplayName("삭제할 User를 찾은 경우")
        public class Context_repository_finds_a_user {
            @BeforeEach
            private void beforeEach() {
                mockFindById()
                    .thenReturn(Optional.of(USER));
            }

            @AfterEach
            private void afterEach() {
                verifyRepository(1)
                    .findById(anyLong());
                verifyRepository(1)
                    .delete(any(User.class));
            }

            @Test
            @DisplayName("User를 삭제한다.")
            public void it_deletes_a_user() {
                subject();
            }
        }
    }

    @Nested
    @DisplayName("updateUser 메서드는")
    public class Describe_updateUser {
        private final User user = mock(User.class);

        private OngoingStubbing<User> mockMapper() {
            return when(dozerMapper.map(any(UpdateUserData.class), eq(User.class)));
        }

        private User subject() {
            return userService.updateUser(ID, UPDATE_USER_DATA);
        }

        private void verifyDomain(final int invokeCounts) {
            verify(user, times(invokeCounts)).update(any(User.class));
        }

        @Nested
        @DisplayName("수정할 User를 찾지 못한 경우")
        public class Context_repository_throws_an_exception {
            @BeforeEach
            private void beforeEach() {
                mockFindById()
                    .thenThrow(new NotFoundException(ID, User.class.getSimpleName()));
            }

            @AfterEach
            private void afterEach() {
                verifyRepository(1)
                    .findById(anyLong());
                verifyDomain(0);
                verifyRepository(0)
                    .save(any(User.class));
            }

            @Test
            @DisplayName("NotFoundException을 던진다.")
            public void it_does_not_catch_exceptions() {
                assertThatThrownBy(() -> subject())
                    .isInstanceOf(NotFoundException.class);
            }
        }

        @Nested
        @DisplayName("수정할 User를 찾은 경우")
        public class Context_repository_finds_a_user {
            @BeforeEach
            private void beforeEach() {
                mockFindById()
                    .thenReturn(Optional.of(user));
            }

            @AfterEach
            private void afterEach() {
                verifyRepository(1)
                    .findById(anyLong());
            }

            @Nested
            @DisplayName("수정할 User데이터를 생성하고")
            public class Context_mapper_generates_data_to_be_updated {
                @BeforeEach
                private void beforeEach() {
                    mockMapper()
                        .thenReturn(USER);
                }

                @AfterEach
                private void afterEach() {
                    verifyMapper(1)
                        .map(any(UpdateUserData.class), eq(User.class));
                }

                @Nested
                @DisplayName("생성한 데이터를 이용하여 User 정보 수정에 성공한 경우")
                public class Context_update_user {
                    @AfterEach
                    private void afterEach() {
                        verifyDomain(1);
                    }

                    @Nested
                    @DisplayName("수정한 User를 저장하고")
                    public class Context_repository_saves_user {
                        @BeforeEach
                        private void beforeEach() {
                            mockSave()
                                .thenReturn(USER);
                        }

                        @AfterEach
                        private void afterEach() {
                            verifyRepository(1)
                                .save(any(User.class));
                        }

                        @Test
                        @DisplayName("리턴한다.")
                        public void it_returns_a_updated_user() {
                            assertThat(subject())
                                .isInstanceOf(User.class);
                        }
                    }
                }
            }
        }
    }
}
