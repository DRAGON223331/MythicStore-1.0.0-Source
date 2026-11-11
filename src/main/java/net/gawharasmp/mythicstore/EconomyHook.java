package net.gawharasmp.mythicstore;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.lang.reflect.Method;

/** Uses Vault's registered Economy service without bundling or shading the Vault API. */
final class EconomyHook {
    private final Class<?> economyClass;
    private final Object provider;

    private EconomyHook(Class<?> economyClass, Object provider) {
        this.economyClass = economyClass;
        this.provider = provider;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static EconomyHook find() {
        try {
            Class<?> api = Class.forName("net.milkbowl.vault.economy.Economy");
            RegisteredServiceProvider<?> registration = Bukkit.getServicesManager()
                    .getRegistration((Class) api);
            if (registration == null || registration.getProvider() == null) return null;
            return new EconomyHook(api, registration.getProvider());
        } catch (ClassNotFoundException exception) {
            return null;
        }
    }

    double balance(Player player) throws ReflectiveOperationException {
        Object result = invoke("getBalance", new Class<?>[]{OfflinePlayer.class}, player);
        return ((Number) result).doubleValue();
    }

    boolean has(Player player, double amount) throws ReflectiveOperationException {
        Object result = invoke("has", new Class<?>[]{OfflinePlayer.class, double.class}, player, amount);
        return Boolean.TRUE.equals(result);
    }

    boolean withdraw(Player player, double amount) throws ReflectiveOperationException {
        Object response = invoke("withdrawPlayer", new Class<?>[]{OfflinePlayer.class, double.class}, player, amount);
        return transactionSucceeded(response);
    }

    void deposit(Player player, double amount) throws ReflectiveOperationException {
        Object response = invoke("depositPlayer", new Class<?>[]{OfflinePlayer.class, double.class}, player, amount);
        if (!transactionSucceeded(response)) {
            throw new IllegalStateException("The economy provider rejected a refund transaction.");
        }
    }

    private Object invoke(String methodName, Class<?>[] parameterTypes, Object... args)
            throws ReflectiveOperationException {
        Method method = economyClass.getMethod(methodName, parameterTypes);
        return method.invoke(provider, args);
    }

    private boolean transactionSucceeded(Object response) throws ReflectiveOperationException {
        if (response == null) return false;
        Method method = response.getClass().getMethod("transactionSuccess");
        return Boolean.TRUE.equals(method.invoke(response));
    }
}
