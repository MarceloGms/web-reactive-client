FROM maven:3-eclipse-temurin-21

# Arguments for user configuration
ARG USER_NAME="dev"
ARG USER_PASSWORD="dev"

# Print the user details (for debugging, can be removed later)
RUN echo $USER_NAME
RUN echo $USER_PASSWORD

# Switch to root user to install additional tools
USER root

# Update and install necessary packages
RUN apt update -y && apt install -y \
    sudo \
    vim \
    zsh \
    less \
    postgresql-client \
    && apt clean all

# Add the user, set password, and add to sudo group
RUN useradd -m -s /bin/zsh -G sudo $USER_NAME \
    && echo "${USER_NAME}:${USER_PASSWORD}" | chpasswd \
    && usermod -aG sudo ${USER_NAME}

# Allow the new user to use sudo without a password (optional for convenience)
RUN echo "${USER_NAME} ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers

# Switch to the new user
USER $USER_NAME

# Set environment variables
ENV TERM xterm
ENV ZSH_THEME robbyrussell

# Install Oh-My-Zsh (non-interactive)
RUN sh -c "$(curl -fsSL https://raw.githubusercontent.com/ohmyzsh/ohmyzsh/master/tools/install.sh)"

# Set zsh as the default shell
CMD ["zsh"]
