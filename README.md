# EUTOPIAVENTS

A comprehensive event organization platform built with Java and JavaFX, offering multiple modules for complete event management, material handling, and community engagement.

## 🚀 Platform Modules

### Event Management (Core)
- Event creation and scheduling
- Venue management
- Participant registration
- Event analytics and reporting
- Calendar integration

### Material Management (materiel)
- Material inventory management
- Material reservation system
- Material usage statistics and analytics
- Admin and user interfaces for material handling
- Role-based access for material operations
- Email notifications
- AI-powered sentiment analysis for material reviews
### Space Management (espaces)
🎯 Admin Features
CRUD operations for venues (lieux)
Categorization of venues (catégories de salle)
Upload and manage photo albums per venue (PhotosLieu)
Geolocation API integration for displaying venue location on an interactive map
Admin dashboard with:
KPIs on venues, categories, and reservations
Visual statistics
PDF export of statistical reports
--‍💼 Client Features
Browse venues by category
search functionality
Venue reservation
Map-based location visualization via geolocation API
### Community & Content (Forum/Blog)
- Blog post management
- Category and post pinning
- Rich text editing
- AI-powered post generation
- Comments and discussions
- User profiles and authentication
- Content moderation
- PDF generation

## 🛠️ Technical Stack

- **Language**: Java
- **Framework**: JavaFX
- **Database**: MySQL
- **Frontend**: FXML, CSS
- **Key Dependencies**: 
  - JDBC for database connectivity
  - JavaMail API for email notifications
  - iText for PDF generation
  - OpenAI API for AI features
  - MQTT for real-time updates

 - Google Maps API

## 📋 Prerequisites

- Java JDK 17+
- Maven
- MySQL/MariaDB
- IDE (IntelliJ IDEA recommended)

## 🚀 Installation

1. Clone the repository:
   ```bash
   git clone [repository-url]
   cd EutopiaVents
   ```

2. Configure your environment:
   - Set up MySQL database
   - Configure database connection in `src/main/utils/DataSource`
   - Set up required API keys for AI features

3. Build the project:
   ```bash
   mvn clean install
   ```

4. Run the application:
   ```bash
   mvn javafx:run
   ```

## 🔧 Configuration

- Database connection settings
- Email server configuration
- OpenAI API credentials
- MQTT broker settings

## 📚 Documentation

See `documentation.txt` for:
- Technical architecture
- Module-specific documentation
- Controllers and services
- Entity relationships
- Forms and repositories
- User interface components
- API endpoints
- Deployment guidelines

## 🧪 Testing

```bash
mvn test
```

## 📦 Deployment

- Set environment variables
- Configure database connection
- Build executable JAR
- Set up Java Runtime Environment
- Configure logging
- Set up monitoring

## 🤝 Contributing

- Fork the repository
- Create your feature branch
- Commit your changes
- Push to the branch
- Create a Pull Request

## 📄 License

Proprietary software. All rights reserved.

## 👥 Authors

- Talel Boukhris
- Youssef Harrane
- Raef Hosni
- Chahnez El Bez
- Kossay Brahim
- Eya Fejjari 
