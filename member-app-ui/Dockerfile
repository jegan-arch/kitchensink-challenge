# ----------------------------
# Stage 1: Build the Application
# ----------------------------
FROM node:18-alpine AS build

WORKDIR /app

# Install dependencies
COPY package.json package-lock.json ./
RUN npm ci

# Copy source and build
COPY . .
RUN npm run build

# ----------------------------
# Stage 2: Serve with Nginx
# ----------------------------
FROM nginx:alpine

# Copy custom Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy built assets from Stage 1
# NOTE: Angular 16+ often puts files in dist/<project-name>
# We copy the contents of that folder to Nginx's serve directory
COPY --from=build /app/dist/kitchensink-ui /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]